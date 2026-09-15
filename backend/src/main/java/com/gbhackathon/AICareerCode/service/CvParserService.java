package com.gbhackathon.AICareerCode.service;

import com.gbhackathon.AICareerCode.dto.ProfileDto;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CvParserService {

    private static final Logger log = LoggerFactory.getLogger(CvParserService.class);

    private static final List<String> KNOWN_SKILLS = List.of(
            "Java", "Spring Boot", "Spring Cloud", "Hibernate", "JPA", "Microservices",
            "Python", "Django", "FastAPI", "Flask", "Machine Learning", "PyTorch", "TensorFlow", "Pandas",
            "JavaScript", "TypeScript", "React", "Next.js", "Vue.js", "Angular", "Node.js", "Express",
            "SQL", "MySQL", "PostgreSQL", "MongoDB", "Redis", "Elasticsearch", "Kafka", "RabbitMQ",
            "Docker", "Kubernetes", "AWS", "Google Cloud", "GCP", "Azure", "CI/CD", "Terraform", "Linux",
            "Git", "RESTful API", "GraphQL", "gRPC", "System Design", "Agile", "Scrum",
            "Golang", "C#", ".NET", "C++", "Swift", "Kotlin", "Flutter", "React Native"
    );

    public String extractTextFromFile(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename != null && filename.toLowerCase().endsWith(".pdf")) {
            try (PDDocument document = Loader.loadPDF(file.getBytes())) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            } catch (Exception e) {
                log.warn("Error parsing PDF via PDFBox, falling back to string conversion: {}", e.getMessage());
                return new String(file.getBytes(), StandardCharsets.UTF_8);
            }
        } else {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        }
    }

    public ProfileDto parseCvTextToProfile(String text) {
        ProfileDto profile = new ProfileDto();
        profile.setRawCvText(text);

        // 1. Email extraction
        Pattern emailPattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}");
        Matcher emailMatcher = emailPattern.matcher(text);
        if (emailMatcher.find()) {
            profile.setEmail(emailMatcher.group());
        }

        // 2. Phone extraction
        Pattern phonePattern = Pattern.compile("(\\+?[0-9]{1,4}[-.\\s]?)?\\(?\\d{3,4}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{3,4}");
        Matcher phoneMatcher = phonePattern.matcher(text);
        if (phoneMatcher.find()) {
            profile.setPhone(phoneMatcher.group().trim());
        }

        // 3. Name heuristic (first non-empty line with letters)
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.length() >= 3 && trimmed.length() <= 40 && !trimmed.contains("@") && !trimmed.matches(".*\\d{5,}.*")) {
                if (!trimmed.equalsIgnoreCase("resume") && !trimmed.equalsIgnoreCase("curriculum vitae") && !trimmed.equalsIgnoreCase("cv")) {
                    profile.setFullName(trimmed);
                    break;
                }
            }
        }
        if (profile.getFullName() == null || profile.getFullName().isBlank()) {
            profile.setFullName("Chuyên viên Công nghệ");
        }

        // 4. Skills extraction
        Set<String> matchedSkills = new LinkedHashSet<>();
        String lowerText = text.toLowerCase();
        for (String skill : KNOWN_SKILLS) {
            Pattern skillPattern = Pattern.compile("\\b" + Pattern.quote(skill.toLowerCase()) + "\\b");
            if (skillPattern.matcher(lowerText).find()) {
                matchedSkills.add(skill);
            }
        }
        if (matchedSkills.isEmpty()) {
            matchedSkills.addAll(List.of("Java", "Spring Boot", "MySQL", "Git", "REST API"));
        }
        profile.setSkills(new ArrayList<>(matchedSkills));

        // 5. Years of experience estimation
        double years = 2.0; // default
        Pattern expPattern = Pattern.compile("(\\d+(\\.\\d+)?)\\+?\\s*(years?|năm)\\s*(of\\s*)?(experience|kinh nghiệm)?", Pattern.CASE_INSENSITIVE);
        Matcher expMatcher = expPattern.matcher(text);
        if (expMatcher.find()) {
            try {
                years = Double.parseDouble(expMatcher.group(1));
            } catch (NumberFormatException ignored) {}
        } else {
            // Count date ranges e.g. 2021 - 2024
            Pattern yearRange = Pattern.compile("20\\d{2}\\s*[-–—]\\s*(20\\d{2}|present|hiện tại|now)", Pattern.CASE_INSENSITIVE);
            Matcher yearMatcher = yearRange.matcher(text);
            int count = 0;
            while (yearMatcher.find()) {
                count++;
            }
            if (count > 0) {
                years = Math.min(count * 1.5, 12.0);
            }
        }
        profile.setYearsOfExperience(years);

        // 6. Current Title heuristic
        String currentTitle = "Software Engineer";
        if (lowerText.contains("backend") || lowerText.contains("java developer") || lowerText.contains("spring")) {
            currentTitle = "Backend Engineer";
        } else if (lowerText.contains("frontend") || lowerText.contains("react") || lowerText.contains("vue")) {
            currentTitle = "Frontend Developer";
        } else if (lowerText.contains("fullstack") || lowerText.contains("full stack") || lowerText.contains("full-stack")) {
            currentTitle = "Full Stack Engineer";
        } else if (lowerText.contains("data engineer") || lowerText.contains("ai") || lowerText.contains("machine learning")) {
            currentTitle = "Data & AI Engineer";
        } else if (lowerText.contains("devops") || lowerText.contains("cloud")) {
            currentTitle = "DevOps & Cloud Engineer";
        }
        profile.setCurrentTitle(currentTitle);

        // 7. Languages
        List<String> langs = new ArrayList<>();
        if (lowerText.contains("english") || lowerText.contains("tiếng anh") || lowerText.contains("ielts") || lowerText.contains("toeic")) {
            langs.add("Tiếng Anh (Giao tiếp tốt)");
        }
        if (lowerText.contains("vietnamese") || lowerText.contains("tiếng việt")) {
            langs.add("Tiếng Việt (Bản ngữ)");
        }
        if (lowerText.contains("japanese") || lowerText.contains("tiếng nhật") || lowerText.contains("n1") || lowerText.contains("n2") || lowerText.contains("n3")) {
            langs.add("Tiếng Nhật");
        }
        if (langs.isEmpty()) {
            langs.add("Tiếng Anh");
            langs.add("Tiếng Việt");
        }
        profile.setLanguages(String.join(", ", langs));

        // 8. Education
        if (lowerText.contains("master") || lowerText.contains("thạc sĩ")) {
            profile.setEducation("Thạc sĩ Khoa học Máy tính / CNTT");
        } else if (lowerText.contains("bachelor") || lowerText.contains("đại học") || lowerText.contains("kỹ sư") || lowerText.contains("cử nhân")) {
            profile.setEducation("Cử nhân Kỹ thuật Phần mềm / CNTT");
        } else {
            profile.setEducation("Cử nhân Công nghệ Thông tin");
        }

        // 9. Default target roles & locations
        profile.setTargetRoles(List.of(currentTitle, "Senior " + currentTitle, "Global Remote Specialist"));
        profile.setTargetLocations(List.of("Việt Nam", "Singapore", "Remote Toàn cầu", "Nhật Bản", "Châu Âu / Đức"));
        profile.setWillingToRelocate(true);
        profile.setTargetWorkType("ANY");
        profile.setBio("Chuyên viên phát triển phần mềm với nền tảng vững chắc về " +
                String.join(", ", profile.getSkills().subList(0, Math.min(4, profile.getSkills().size()))) +
                ", mong muốn tìm kiếm cơ hội bứt phá nghề nghiệp trong nước và quốc tế.");

        return profile;
    }
}
