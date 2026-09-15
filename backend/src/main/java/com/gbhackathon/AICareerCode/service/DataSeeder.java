package com.gbhackathon.AICareerCode.service;

import com.gbhackathon.AICareerCode.model.JobOpportunity;
import com.gbhackathon.AICareerCode.repository.JobOpportunityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private final JobOpportunityRepository jobRepository;

    public DataSeeder(JobOpportunityRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Override
    public void run(String... args) {
        if (jobRepository.count() > 0) {
            log.info("Database already contains job data ({} jobs). Skipping seeding.", jobRepository.count());
            return;
        }

        log.info("Seeding initial tech job opportunities (Domestic & Overseas)...");

        List<JobOpportunity> jobs = List.of(
                // 1. Overseas - Singapore (Visa Sponsored)
                createJob(
                        "Senior Backend Engineer (Distributed Systems)",
                        "Grab Financial Group",
                        "https://images.unsplash.com/photo-1542744173-8e7e53415bb0?w=120&auto=format&fit=crop&q=60",
                        "Marina One, Singapore",
                        "Singapore",
                        true,
                        "HYBRID",
                        "SGD 8,500 - 12,000 / month",
                        "Senior (5+ yrs)",
                        4,
                        "Java, Spring Boot, Microservices, Redis, Kafka, MySQL, System Design",
                        "Kubernetes, AWS, Golang, gRPC",
                        true,
                        true,
                        "English (Professional Working Proficiency)",
                        "Tham gia xây dựng kiến trúc ví điện tử và cổng thanh toán quy mô Đông Nam Á phục vụ hơn 30 triệu người dùng.",
                        "Tối thiểu 4 năm kinh nghiệm với Java/Spring Boot hoặc Go, có kinh nghiệm xử lý giao dịch cao tải và kiến trúc vi dịch vụ.",
                        "Hỗ trợ Employment Pass (EP) Visa toàn bộ chi phí, vé máy bay và trợ cấp 1 tháng nhà ở tại Singapore.",
                        "https://grab.careers",
                        "LinkedIn"
                ),

                // 2. Overseas - Germany (EU Blue Card)
                createJob(
                        "Full Stack Cloud Engineer (Java & React)",
                        "Zalando SE",
                        "https://images.unsplash.com/photo-1572021335469-31706a17aaef?w=120&auto=format&fit=crop&q=60",
                        "Berlin, Germany",
                        "Germany",
                        true,
                        "HYBRID",
                        "EUR 70,000 - 90,000 / year",
                        "Mid to Senior (3-5 yrs)",
                        3,
                        "Java, Spring Boot, React, TypeScript, Docker, AWS",
                        "PostgreSQL, Kubernetes, Next.js, GraphQL",
                        true,
                        true,
                        "English (Fluent) - German is not required",
                        "Phát triển nền tảng thương mại điện tử thời trang lớn nhất châu Âu với hơn 50 triệu khách hàng.",
                        "Kinh nghiệm vững chắc về Java Spring Boot và React/TypeScript; tư duy Clean Architecture.",
                        "Bảo lãnh thẻ xanh châu Âu (EU Blue Card), gói hỗ trợ chuyển nhà (Relocation Allowance EUR 5,000) và khóa học tiếng Đức miễn phí.",
                        "https://jobs.zalando.com",
                        "Relocate.me"
                ),

                // 3. Overseas - Japan (Tokyo)
                createJob(
                        "Cloud & Backend Specialist (Global Team)",
                        "Rakuten Group",
                        "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=120&auto=format&fit=crop&q=60",
                        "Tokyo, Japan",
                        "Japan",
                        true,
                        "HYBRID",
                        "JPY 7,000,000 - 10,000,000 / year",
                        "Mid-level (3+ yrs)",
                        3,
                        "Java, Spring Boot, MySQL, Docker, Linux, REST API",
                        "Kubernetes, CI/CD, Redis, Elasticsearch, Python",
                        true,
                        true,
                        "English (Official working language at Rakuten) - Japanese is a plus",
                        "Xây dựng hạ tầng fintech và thương mại điện tử toàn cầu tại trụ sở Rakuten Crimson House Tokyo.",
                        "Tốt nghiệp đại học chuyên ngành CNTT; tiếng Anh lưu loát; thành thạo hệ sinh thái Java.",
                        "Hỗ trợ xin visa Kỹ sư Nhật Bản, vé máy bay sang Tokyo và ký túc xá/căn hộ hỗ trợ ban đầu.",
                        "https://rakuten.careers",
                        "Daijob"
                ),

                // 4. Remote Global (Work from Vietnam)
                createJob(
                        "Senior Platform Engineer (100% Remote Worldwide)",
                        "GitLab / Remote Tech Partners",
                        "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=120&auto=format&fit=crop&q=60",
                        "Remote Worldwide",
                        "Global",
                        true,
                        "REMOTE",
                        "$5,000 - $7,500 / month ($60k - $90k/yr)",
                        "Senior (4+ yrs)",
                        4,
                        "Docker, Kubernetes, AWS, CI/CD, Terraform, Python, Linux",
                        "Go, Prometheus, Grafana, Java",
                        false,
                        false,
                        "English (Excellent written and verbal communication)",
                        "Vận hành và mở rộng quy mô hạ tầng Cloud native toàn cầu, làm việc tự do theo múi giờ linh hoạt từ bất kỳ đâu.",
                        "Kinh nghiệm chuyên sâu với Kubernetes, Infrastructure as Code (Terraform) và tự động hóa CI/CD.",
                        "Lương chuẩn USD thanh toán trực tiếp về Việt Nam; ngân sách $2,000 thiết lập trang thiết bị làm việc tại nhà.",
                        "https://about.gitlab.com/jobs",
                        "Wellfound"
                ),

                // 5. Overseas - Australia (Sydney)
                createJob(
                        "Software Engineer - Core Services",
                        "Canva",
                        "https://images.unsplash.com/photo-1551836022-d5d88e9218df?w=120&auto=format&fit=crop&q=60",
                        "Sydney, Australia",
                        "Australia",
                        true,
                        "HYBRID",
                        "AUD 120,000 - 150,000 / year",
                        "Mid to Senior (3-6 yrs)",
                        3,
                        "Java, TypeScript, React, AWS, Microservices, System Design",
                        "Redis, DynamoDB, Distributed Caching",
                        true,
                        true,
                        "English (IELTS 6.5+ equivalent for TSS 482 Visa)",
                        "Tham gia phát triển công cụ đồ họa trực tuyến phục vụ hơn 130 triệu người dùng hàng tháng.",
                        "Nền tảng vững vàng về thuật toán, cấu trúc dữ liệu và thiết kế hệ thống phân tán chịu tải cao.",
                        "Bảo lãnh Temporary Skill Shortage (TSS) Visa subclass 482 sang định cư Úc cùng gia đình.",
                        "https://canva.com/careers",
                        "LinkedIn"
                ),

                // 6. Domestic - Vietnam (Ho Chi Minh City)
                createJob(
                        "Lead Backend Engineer (High-throughput Systems)",
                        "VNG Corporation",
                        "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=120&auto=format&fit=crop&q=60",
                        "Zalo Campus, Quận 7, TP. Hồ Chí Minh",
                        "Vietnam",
                        false,
                        "HYBRID",
                        "45,000,000 - 75,000,000 VND / tháng ($1,800 - $3,000)",
                        "Senior (4+ yrs)",
                        4,
                        "Java, Spring Boot, MySQL, Redis, Kafka, Microservices",
                        "Docker, Kubernetes, Elasticsearch, System Design",
                        false,
                        false,
                        "Tiếng Việt (Bản ngữ), Tiếng Anh (Đọc hiểu tài liệu & giao tiếp)",
                        "Chịu trách nhiệm kiến trúc và tối ưu hóa các module thanh toán điện tử & messaging cho hàng chục triệu người dùng Việt Nam.",
                        "Khả năng thiết kế hệ thống mở rộng, hiểu sâu về concurrency, transaction isolation và tối ưu truy vấn cơ sở dữ liệu lớn.",
                        "Thưởng hiệu quả hàng năm 3-5 tháng lương; bảo hiểm chăm sóc sức khỏe quốc tế cao cấp; phòng gym, yoga tại Zalo Campus.",
                        "https://career.vng.com.vn",
                        "VietnamWorks"
                ),

                // 7. Domestic - Vietnam (Hanoi)
                createJob(
                        "Cloud Solutions & DevOps Architect",
                        "FPT Software Global Delivery",
                        "https://images.unsplash.com/photo-1497366216548-37526070297c?w=120&auto=format&fit=crop&q=60",
                        "FPT Tower, Cầu Giấy, Hà Nội",
                        "Vietnam",
                        false,
                        "HYBRID",
                        "35,000,000 - 55,000,000 VND / tháng",
                        "Mid to Senior (3+ yrs)",
                        3,
                        "AWS, Docker, Kubernetes, CI/CD, Linux, Terraform",
                        "Java, Python, Azure, GCP",
                        false,
                        false,
                        "Tiếng Anh (Giao tiếp tốt với khách hàng Mỹ/Âu)",
                        "Thiết kế và triển khai kiến trúc Cloud Migration cho các tập đoàn đa quốc gia trong danh sách Fortune 500.",
                        "Có các chứng chỉ AWS Solutions Architect Associate/Professional là lợi thế lớn.",
                        "Cơ hội onsite ngắn và dài hạn tại Mỹ, Nhật Bản, châu Âu; lộ trình thăng tiến rõ ràng lên Solution Architect.",
                        "https://career.fpt-software.com",
                        "TopCV"
                ),

                // 8. Domestic - Vietnam (Fintech)
                createJob(
                        "Senior Frontend Engineer (React / Next.js)",
                        "MoMo Super App (M_Service)",
                        "https://images.unsplash.com/photo-1556742049-0a67c5574f73?w=120&auto=format&fit=crop&q=60",
                        "Quận 7, TP. Hồ Chí Minh",
                        "Vietnam",
                        false,
                        "HYBRID",
                        "40,000,000 - 65,000,000 VND / tháng",
                        "Senior (3+ yrs)",
                        3,
                        "React, TypeScript, Next.js, JavaScript, REST API, Git",
                        "Tailwind CSS, Redux, Performance Optimization, Unit Testing",
                        false,
                        false,
                        "Tiếng Việt, Tiếng Anh giao tiếp",
                        "Phát triển các mini-app và trải nghiệm giao diện người dùng mượt mà trên hệ sinh thái siêu ứng dụng MoMo.",
                        "Thành thạo React, tối ưu hiệu năng render, Web Vitals và thiết kế UI/UX theo tiêu chuẩn Mobile-First.",
                        "Môi trường công nghệ năng động, ESOP cổ phiếu thưởng cho nhân sự chủ chốt.",
                        "https://momo.vn/tuyen-dung",
                        "ITviec"
                ),

                // 9. Remote - US Startup
                createJob(
                        "Full Stack Developer (React & Node/Java)",
                        "Silicon Valley Seed Startup",
                        "https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=120&auto=format&fit=crop&q=60",
                        "Remote (US East Coast timezone overlap)",
                        "United States",
                        true,
                        "REMOTE",
                        "$4,000 - $6,000 / month ($48k - $72k/yr)",
                        "Mid-level (2-4 yrs)",
                        2,
                        "React, TypeScript, Java, Spring Boot, MySQL, REST API",
                        "AWS, Docker, Tailwind CSS, Redis",
                        false,
                        false,
                        "English (Fluent speaking & writing)",
                        "Trực tiếp xây dựng sản phẩm AI SaaS phục vụ thị trường B2B doanh nghiệp Mỹ.",
                        "Chủ động, có tinh thần khởi nghiệp cao, khả năng làm việc độc lập tốt và tiếng Anh trôi chảy.",
                        "Lương USD ổn định, làm việc tại nhà 100%, thưởng equity/token khởi nghiệp.",
                        "https://angel.co",
                        "AngelList"
                )
        );

        jobRepository.saveAll(jobs);
        log.info("Successfully seeded {} diverse tech job opportunities!", jobs.size());
    }

    private JobOpportunity createJob(
            String title, String company, String logo, String location, String country,
            boolean isOverseas, String workType, String salary, String expLevel, int minExp,
            String reqSkills, String prefSkills, boolean visa, boolean reloc, String lang,
            String desc, String reqs, String benefits, String url, String source
    ) {
        JobOpportunity j = new JobOpportunity();
        j.setTitle(title);
        j.setCompany(company);
        j.setCompanyLogo(logo);
        j.setLocation(location);
        j.setCountry(country);
        j.setIsOverseas(isOverseas);
        j.setWorkType(workType);
        j.setSalaryRange(salary);
        j.setExperienceLevel(expLevel);
        j.setMinYearsExp(minExp);
        j.setRequiredSkills(reqSkills);
        j.setPreferredSkills(prefSkills);
        j.setVisaSponsorship(visa);
        j.setRelocationAssistance(reloc);
        j.setLanguageRequirements(lang);
        j.setDescription(desc);
        j.setRequirements(reqs);
        j.setBenefits(benefits);
        j.setApplyUrl(url);
        j.setSource(source);
        j.setPostedAt(LocalDateTime.now().minusDays((long) (Math.random() * 10)));
        return j;
    }
}
