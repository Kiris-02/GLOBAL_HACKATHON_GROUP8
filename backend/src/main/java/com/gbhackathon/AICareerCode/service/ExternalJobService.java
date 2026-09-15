package com.gbhackathon.AICareerCode.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gbhackathon.AICareerCode.model.JobOpportunity;
import com.gbhackathon.AICareerCode.repository.JobOpportunityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class ExternalJobService {

    private static final Logger log = LoggerFactory.getLogger(ExternalJobService.class);
    private final JobOpportunityRepository jobRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExternalJobService(JobOpportunityRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        // Automatically sync on startup if database has less than 15 jobs
        long count = jobRepository.count();
        if (count <= 10) {
            log.info("Initial database has {} jobs. Automatically fetching live jobs from external APIs...", count);
            try {
                syncExternalJobs();
            } catch (Exception e) {
                log.warn("Failed to sync external jobs on startup: {}", e.getMessage());
            }
        }
    }

    @Transactional
    public Map<String, Object> syncExternalJobs() {
        int arbeitnowCount = 0;
        int remotiveCount = 0;

        // 1. Fetch from Arbeitnow (European Tech Jobs & Visa Sponsorship)
        try {
            arbeitnowCount = fetchArbeitnowJobs();
        } catch (Exception e) {
            log.error("Error fetching from Arbeitnow API: {}", e.getMessage());
        }

        // 2. Fetch from Remotive (Global Remote Tech Jobs with USD Salaries)
        try {
            remotiveCount = fetchRemotiveJobs();
        } catch (Exception e) {
            log.error("Error fetching from Remotive API: {}", e.getMessage());
        }

        long total = jobRepository.count();
        return Map.of(
                "success", true,
                "message", String.format("Đã đồng bộ thành công %d việc làm từ Arbeitnow (EU/Visa) và %d việc làm từ Remotive (Remote Global).", arbeitnowCount, remotiveCount),
                "totalJobsInDatabase", total
        );
    }

    private int fetchArbeitnowJobs() {
        String url = "https://www.arbeitnow.com/api/job-board-api";
        RestClient client = RestClient.builder().build();

        String response = client.get()
                .uri(url)
                .retrieve()
                .body(String.class);

        int added = 0;
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode data = root.path("data");
            if (data.isArray()) {
                for (JsonNode node : data) {
                    String title = node.path("title").asText("");
                    String company = node.path("company_name").asText("Tech Company");
                    String applyUrl = node.path("url").asText("");

                    // Check if job already exists by title and company
                    if (title.isBlank() || jobRepository.searchJobs(title, null, null, null).stream().anyMatch(j -> j.getCompany().equalsIgnoreCase(company))) {
                        continue;
                    }

                    JobOpportunity job = new JobOpportunity();
                    job.setTitle(title);
                    job.setCompany(company);
                    job.setCompanyLogo("https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=120&auto=format&fit=crop&q=60");

                    String location = node.path("location").asText("Germany");
                    job.setLocation(location);
                    job.setCountry("Germany / Europe");
                    job.setIsOverseas(true);

                    boolean remote = node.path("remote").asBoolean(false);
                    job.setWorkType(remote ? "REMOTE" : "HYBRID");

                    boolean visa = node.path("visa_sponsorship").asBoolean(false);
                    job.setVisaSponsorship(visa);
                    job.setRelocationAssistance(visa);

                    job.setSalaryRange("EUR 60,000 - 85,000 / year");
                    job.setExperienceLevel("Mid to Senior (3-5 yrs)");
                    job.setMinYearsExp(3);

                    // Extract tags for skills
                    List<String> skills = new ArrayList<>();
                    JsonNode tags = node.path("tags");
                    if (tags.isArray()) {
                        for (JsonNode t : tags) {
                            String skill = t.asText().trim();
                            if (!skill.equalsIgnoreCase("remote") && !skill.equalsIgnoreCase("full time")) {
                                skills.add(skill);
                            }
                        }
                    }
                    if (skills.isEmpty()) {
                        skills.addAll(List.of("Java", "React", "Docker", "SQL", "Git"));
                    }
                    job.setRequiredSkills(String.join(", ", skills.subList(0, Math.min(6, skills.size()))));
                    job.setPreferredSkills("AWS, Kubernetes, CI/CD, Agile");

                    job.setLanguageRequirements("English (Professional Working)");
                    String rawDesc = node.path("description").asText("");
                    // Strip HTML tags for clean display
                    String cleanDesc = rawDesc.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
                    if (cleanDesc.length() > 600) {
                        cleanDesc = cleanDesc.substring(0, 600) + "...";
                    }
                    job.setDescription(cleanDesc.isEmpty() ? "Tham gia phát triển sản phẩm công nghệ quốc tế tại châu Âu." : cleanDesc);
                    job.setRequirements("Tối thiểu 3 năm kinh nghiệm trong lĩnh vực công nghệ thông tin; tư duy Clean Code và giải quyết vấn đề độc lập.");
                    job.setBenefits("Môi trường làm việc quốc tế 100% tiếng Anh; hỗ trợ chi phí chuyển vùng định cư (nếu có tài trợ visa); bảo hiểm y tế toàn diện.");
                    job.setApplyUrl(applyUrl);
                    job.setSource("Arbeitnow API (EU)");
                    job.setPostedAt(LocalDateTime.now());

                    jobRepository.save(job);
                    added++;
                    if (added >= 10) break; // Limit per sync batch
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse Arbeitnow response: {}", e.getMessage());
        }
        return added;
    }

    private int fetchRemotiveJobs() {
        String url = "https://remotive.com/api/remote-jobs?category=software-dev&limit=25";
        RestClient client = RestClient.builder().build();

        String response = client.get()
                .uri(url)
                .retrieve()
                .body(String.class);

        int added = 0;
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode jobs = root.path("jobs");
            if (jobs.isArray()) {
                for (JsonNode node : jobs) {
                    String title = node.path("title").asText("");
                    String company = node.path("company_name").asText("Global Tech");
                    String applyUrl = node.path("url").asText("");

                    if (title.isBlank() || jobRepository.searchJobs(title, null, null, null).stream().anyMatch(j -> j.getCompany().equalsIgnoreCase(company))) {
                        continue;
                    }

                    JobOpportunity job = new JobOpportunity();
                    job.setTitle(title);
                    job.setCompany(company);
                    job.setCompanyLogo("https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=120&auto=format&fit=crop&q=60");

                    String reqLoc = node.path("candidate_required_location").asText("Worldwide");
                    job.setLocation("Remote (" + reqLoc + ")");
                    job.setCountry(reqLoc.contains("USA") ? "United States" : "Global");
                    job.setIsOverseas(true);
                    job.setWorkType("REMOTE");

                    String salary = node.path("salary").asText("");
                    if (salary.isBlank()) {
                        salary = "$4,500 - $7,000 / month ($54k - $84k/yr)";
                    }
                    job.setSalaryRange(salary);
                    job.setExperienceLevel("Senior (4+ yrs)");
                    job.setMinYearsExp(3);

                    // Extract skills from tags
                    List<String> skills = new ArrayList<>();
                    JsonNode tags = node.path("tags");
                    if (tags.isArray()) {
                        for (JsonNode t : tags) {
                            String skill = t.asText().trim();
                            if (skill.length() <= 20) {
                                skills.add(skill);
                            }
                        }
                    }
                    if (skills.isEmpty()) {
                        skills.addAll(List.of("React", "TypeScript", "Node.js", "Docker", "REST API"));
                    }
                    job.setRequiredSkills(String.join(", ", skills.subList(0, Math.min(6, skills.size()))));
                    job.setPreferredSkills("Microservices, Cloud, DevOps, Unit Testing");

                    job.setVisaSponsorship(false);
                    job.setRelocationAssistance(false);
                    job.setLanguageRequirements("English (Fluent verbal & written)");

                    String rawDesc = node.path("description").asText("");
                    String cleanDesc = rawDesc.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
                    if (cleanDesc.length() > 600) {
                        cleanDesc = cleanDesc.substring(0, 600) + "...";
                    }
                    job.setDescription(cleanDesc.isEmpty() ? "Làm việc từ xa 100% cho sản phẩm công nghệ toàn cầu." : cleanDesc);
                    job.setRequirements("Kinh nghiệm làm việc độc lập tốt trong môi trường Agile/Scrum phân tán toàn cầu; khả năng giao tiếp tiếng Anh trôi chảy.");
                    job.setBenefits("Thu nhập cạnh tranh bằng USD; tự do lựa chọn nơi làm việc tại nhà; ngân sách trang bị thiết bị văn phòng và khóa học công nghệ.");
                    job.setApplyUrl(applyUrl);
                    job.setSource("Remotive API (Global)");
                    job.setPostedAt(LocalDateTime.now());

                    jobRepository.save(job);
                    added++;
                    if (added >= 10) break;
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse Remotive response: {}", e.getMessage());
        }
        return added;
    }
}
