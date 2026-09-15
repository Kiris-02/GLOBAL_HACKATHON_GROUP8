package com.gbhackathon.AICareerCode.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gbhackathon.AICareerCode.dto.CareerRoadmapDto;
import com.gbhackathon.AICareerCode.dto.ChatMessageDto;
import com.gbhackathon.AICareerCode.dto.ResumeAuditDto;
import com.gbhackathon.AICareerCode.model.JobOpportunity;
import com.gbhackathon.AICareerCode.model.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

@Service
public class AiCoachService {

    private static final Logger log = LoggerFactory.getLogger(AiCoachService.class);

    @Value("${ai.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${ai.gemini.model:gemini-1.5-flash}")
    private String geminiModel;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ResumeAuditDto auditProfile(UserProfile profile) {
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            try {
                ResumeAuditDto geminiAudit = callGeminiForAudit(profile);
                if (geminiAudit != null && geminiAudit.getHealthScore() > 0) {
                    log.info("Generated dynamic Resume Audit with Gemini 3.5 Flash for user: {}", profile.getFullName());
                    return geminiAudit;
                }
            } catch (Exception e) {
                log.warn("Gemini audit call failed, using heuristic fallback: {}", e.getMessage());
            }
        }
        return generateHeuristicAudit(profile);
    }

    private ResumeAuditDto callGeminiForAudit(UserProfile profile) {
        String url = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                geminiModel, geminiApiKey);

        String prompt = String.format("""
                You are a world-class Technical Recruiter & Career Strategist evaluating a tech candidate's profile for top domestic and international engineering roles (Singapore, Germany, US, Global Remote).
                
                Candidate Profile:
                - Name: %s
                - Current Title: %s
                - Years of Experience: %.1f
                - Core Skills: %s
                - Target Roles: %s
                - Target Locations: %s
                - Willing to Relocate: %s
                - Education: %s
                - Languages: %s
                - Bio / Summary: %s
                
                Generate a comprehensive, deeply personalized Resume Audit & 12-Month Progression Roadmap in English.
                You must output STRICT JSON matching this exact structure:
                {
                  "healthScore": 82,
                  "verdict": "High ATS Compliance - Strong Profile for Global Opportunities",
                  "summary": "Detailed 2-3 sentences evaluating candidate's market readiness, key strengths, and highest-ROI improvement area.",
                  "strengths": [
                    "Specific technical strength 1",
                    "Specific technical strength 2",
                    "Specific mobility or foundation strength 3"
                  ],
                  "weaknesses": [
                    "Specific actionable gap 1",
                    "Specific actionable gap 2",
                    "Specific actionable gap 3"
                  ],
                  "atsKeywordsPresent": ["Skill1", "Skill2", "Skill3"],
                  "atsKeywordsMissing": ["MissingCloudSkill", "MissingArchitectureSkill", "MissingTool"],
                  "bulletImprovements": [
                    {
                      "originalBullet": "Common weak resume bullet for this role",
                      "improvedBullet": "STAR formula bullet with concrete metrics (%%, scale, latency, users) tailored to candidate's actual stack",
                      "explanation": "Why this revised bullet commands attention from global hiring managers"
                    },
                    {
                      "originalBullet": "Another weak resume bullet",
                      "improvedBullet": "Another high-impact STAR formula bullet with quantifiable metrics",
                      "explanation": "Recruiter perspective explanation"
                    }
                  ],
                  "careerRoadmap": {
                    "targetGoal": "Achieve Senior/Staff Engineer or Global Remote Offer",
                    "months3": [
                      {
                        "title": "Milestone title",
                        "description": "Concrete action details",
                        "type": "SKILL",
                        "estimatedHours": "40 hrs"
                      },
                      {
                        "title": "Milestone title",
                        "description": "Concrete action details",
                        "type": "LANGUAGE",
                        "estimatedHours": "30 hrs"
                      }
                    ],
                    "months6": [
                      {
                        "title": "Milestone title",
                        "description": "Concrete action details",
                        "type": "CERTIFICATION",
                        "estimatedHours": "60 hrs"
                      },
                      {
                        "title": "Milestone title",
                        "description": "Concrete action details",
                        "type": "PROJECT",
                        "estimatedHours": "50 hrs"
                      }
                    ],
                    "months12": [
                      {
                        "title": "Milestone title",
                        "description": "Concrete action details",
                        "type": "APPLICATION",
                        "estimatedHours": "40 hrs"
                      },
                      {
                        "title": "Milestone title",
                        "description": "Concrete action details",
                        "type": "INTERVIEW",
                        "estimatedHours": "50 hrs"
                      }
                    ]
                  }
                }
                """,
                profile.getFullName(), profile.getCurrentTitle(),
                profile.getYearsOfExperience() != null ? profile.getYearsOfExperience() : 1.0,
                profile.getSkills(), profile.getTargetRoles(), profile.getTargetLocations(),
                Boolean.TRUE.equals(profile.getWillingToRelocate()) ? "Yes" : "No",
                profile.getEducation(), profile.getLanguages(),
                profile.getBio()
        );

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                ),
                "generationConfig", Map.of(
                        "response_mime_type", "application/json"
                )
        );

        RestClient client = RestClient.create();
        String jsonResult = client.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(jsonResult);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                String text = candidates.get(0).path("content").path("parts").get(0).path("text").asText();
                return objectMapper.readValue(text, ResumeAuditDto.class);
            }
        } catch (Exception e) {
            log.error("Failed to parse Gemini audit response JSON: {}", e.getMessage());
        }
        return null;
    }

    public ResumeAuditDto generateHeuristicAudit(UserProfile profile) {
        ResumeAuditDto audit = new ResumeAuditDto();
        List<String> skills = profile.getSkillList();
        double exp = profile.getYearsOfExperience() != null ? profile.getYearsOfExperience() : 1.0;

        // Calculate health score (60 - 95 based on profile richness)
        int score = 65;
        if (skills.size() >= 5) score += 10;
        if (skills.size() >= 8) score += 5;
        if (exp >= 3.0) score += 5;
        if (profile.getEducation() != null && !profile.getEducation().isBlank()) score += 5;
        if (profile.getLanguages() != null && profile.getLanguages().toLowerCase().contains("tiếng anh")) score += 5;
        score = Math.min(95, score);

        audit.setHealthScore(score);
        if (score >= 85) {
            audit.setVerdict("High ATS Compliance - Ready for Global Opportunities");
        } else if (score >= 70) {
            audit.setVerdict("Solid Profile Foundation - Add Measurable Business Metrics");
        } else {
            audit.setVerdict("Fundamental Profile - Requires Structural & Keyword Overhaul");
        }

        audit.setSummary(String.format("Your profile demonstrates strong foundations in %s with %.1f years of professional experience. To effectively compete with global candidates in Singapore, Germany, and Japan, emphasize quantifiable business metrics (e.g., reduced latency by X%%, increased throughput by Y%%) and pursue industry-recognized cloud certifications.",
                skills.isEmpty() ? "Software Engineering" : skills.get(0), exp));

        // Strengths
        List<String> strengths = new ArrayList<>();
        strengths.add("Clear, coherent core technical skill set: " + String.join(", ", skills.subList(0, Math.min(4, skills.size()))));
        strengths.add("Strong educational background and well-defined role specialization (" + profile.getCurrentTitle() + ").");
        if (Boolean.TRUE.equals(profile.getWillingToRelocate())) {
            strengths.add("High mobility mindset and active openness to international relocation & global tech markets.");
        }
        audit.setStrengths(strengths);

        // Weaknesses
        List<String> weaknesses = new ArrayList<>();
        weaknesses.add("Lacks quantifiable impact metrics (%, $, user scale, performance multipliers) across experience descriptions.");
        weaknesses.add("Cloud infrastructure (AWS/GCP), automated CI/CD pipelines, and distributed microservices architecture can be emphasized more prominently.");
        weaknesses.add("Add a concise, high-impact 1-page English career summary tailored for international hiring standards.");
        audit.setWeaknesses(weaknesses);

        // ATS Keywords Present vs Missing
        List<String> atsPresent = new ArrayList<>(skills);
        List<String> popularAts = List.of("System Design", "Microservices", "Docker", "Kubernetes", "AWS", "CI/CD", "Performance Optimization", "Agile/Scrum", "Unit Testing", "Kafka");
        List<String> atsMissing = new ArrayList<>();
        for (String kw : popularAts) {
            if (!skills.contains(kw) && atsMissing.size() < 5) {
                atsMissing.add(kw);
            }
        }
        audit.setAtsKeywordsPresent(atsPresent);
        audit.setAtsKeywordsMissing(atsMissing);

        // Bullet point improvements
        List<ResumeAuditDto.BulletImprovement> bullets = new ArrayList<>();
        bullets.add(new ResumeAuditDto.BulletImprovement(
                "Developed backend APIs for applications and maintained databases.",
                "Architected and optimized 15+ RESTful microservices using Spring Boot & MySQL, reducing API query latency by 35% and serving 100,000+ daily active users.",
                "Employs the STAR formula with concrete metrics (15+ APIs, 35% latency, 100k DAU) allowing global recruiters to immediately gauge real-world impact."
        ));
        bullets.add(new ResumeAuditDto.BulletImprovement(
                "Fixed system bugs and collaborated with QA testing team.",
                "Spearheaded resolution of 40+ critical security vulnerabilities and established automated test suites with JUnit & Mockito achieving 80% code coverage, cutting release cycles by 20%.",
                "Transforms passive maintenance into proactive, measurable engineering ownership (80% coverage, 20% faster release cycle)."
        ));
        bullets.add(new ResumeAuditDto.BulletImprovement(
                "Used Docker to package and deploy applications to servers.",
                "Automated containerized CI/CD deployment pipelines using Docker and GitHub Actions, standardizing Staging/Production environments and slashing deployment time from 45 min to under 5 min.",
                "Demonstrates modern DevOps maturity and automation acumen—a key evaluation criteria for overseas engineering teams."
        ));
        audit.setBulletImprovements(bullets);

        // Career Roadmap
        CareerRoadmapDto roadmap = generateCareerRoadmap(profile);
        audit.setCareerRoadmap(roadmap);

        return audit;
    }

    public CareerRoadmapDto generateCareerRoadmap(UserProfile profile) {
        CareerRoadmapDto roadmap = new CareerRoadmapDto();
        roadmap.setTargetGoal("Achieve Senior / Global Software Engineer Offer in Domestic or Overseas Tech Markets");

        List<CareerRoadmapDto.RoadmapMilestone> m3 = new ArrayList<>();
        m3.add(new CareerRoadmapDto.RoadmapMilestone(
                "Close Critical Skill Gaps & ATS Resume Polishing",
                "Master advanced Docker/Kubernetes containerization, Redis caching, and reformat all resume bullet points using the STAR methodology.",
                "SKILL", "40 hrs"));
        m3.add(new CareerRoadmapDto.RoadmapMilestone(
                "Professional Technical English Communication",
                "Practice English technical mock interviews focusing on architectural trade-offs and behavioral scenarios (Amazon Leadership Principles).",
                "LANGUAGE", "30 hrs"));
        roadmap.setMonths3(m3);

        List<CareerRoadmapDto.RoadmapMilestone> m6 = new ArrayList<>();
        m6.add(new CareerRoadmapDto.RoadmapMilestone(
                "Global Cloud Certification (AWS / GCP)",
                "Earn AWS Certified Solutions Architect Associate or Google Cloud Associate Cloud Engineer credential.",
                "CERTIFICATION", "60 hrs"));
        m6.add(new CareerRoadmapDto.RoadmapMilestone(
                "Open-Source Production-Grade Portfolio Project",
                "Build an open-source distributed microservices showcase on GitHub featuring CI/CD automation, Kafka event streaming, and comprehensive English documentation.",
                "PROJECT", "50 hrs"));
        roadmap.setMonths6(m6);

        List<CareerRoadmapDto.RoadmapMilestone> m12 = new ArrayList<>();
        m12.add(new CareerRoadmapDto.RoadmapMilestone(
                "Targeted International Application Campaign",
                "Submit 20-30 targeted applications in Singapore, Berlin, Tokyo, and Worldwide Remote teams on LinkedIn, Otta, and Wellfound; prepare visa documentation.",
                "APPLICATION", "40 hrs"));
        m12.add(new CareerRoadmapDto.RoadmapMilestone(
                "Advanced Algorithms & System Design Mastery",
                "Complete 100 LeetCode Medium/Hard challenges and master core System Design blueprints (Rate Limiter, Distributed Cache, Real-time Messaging).",
                "INTERVIEW", "50 hrs"));
        roadmap.setMonths12(m12);

        return roadmap;
    }

    public String chat(UserProfile profile, List<ChatMessageDto> history, String userPrompt) {
        // If Gemini API Key is configured, try calling Gemini LLM
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            try {
                String response = callGeminiApi(profile, history, userPrompt);
                if (response != null && !response.isBlank()) {
                    return response;
                }
            } catch (Exception e) {
                log.warn("Gemini API call failed, falling back to built-in smart AI coach: {}", e.getMessage());
            }
        }

        // Built-in Smart AI Career Coach Fallback Engine
        return generateHeuristicCoachResponse(profile, userPrompt);
    }

    private String callGeminiApi(UserProfile profile, List<ChatMessageDto> history, String userPrompt) {
        String url = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                geminiModel, geminiApiKey);

        String systemInstruction = String.format(
                "You are an expert AI Career Coach specializing in tech careers, global relocation, resume ATS optimization, and interview preparation. " +
                "Current candidate profile: Name: %s, Title: %s, Experience: %.1f years, Skills: %s, Target Roles: %s, Target Locations: %s. " +
                "Provide practical, actionable advice on career growth, resume bullet points (using the STAR method with metrics), mock interview questions, and visa/relocation guidance. " +
                "Respond in well-structured, inspiring Markdown format in the same language as the user's inquiry (English by default, or Vietnamese if queried in Vietnamese).",
                profile.getFullName(), profile.getCurrentTitle(),
                profile.getYearsOfExperience() != null ? profile.getYearsOfExperience() : 1.0,
                profile.getSkills(), profile.getTargetRoles(), profile.getTargetLocations()
        );

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append(systemInstruction).append("\n\n");
        if (history != null) {
            for (ChatMessageDto msg : history) {
                promptBuilder.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n");
            }
        }
        promptBuilder.append("user: ").append(userPrompt);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", promptBuilder.toString())
                        ))
                )
        );

        RestClient client = RestClient.create();
        String jsonResult = client.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(jsonResult);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                return candidates.get(0).path("content").path("parts").get(0).path("text").asText();
            }
        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", e.getMessage());
        }
        return null;
    }

    public Map<String, Object> generateJobDeepDive(UserProfile profile, JobOpportunity job) {
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            try {
                Map<String, Object> geminiAnalysis = callGeminiForJobDeepDive(profile, job);
                if (geminiAnalysis != null && !geminiAnalysis.isEmpty()) {
                    log.info("Generated dynamic Job Deep Dive with Gemini 3.5 Flash for: {}", job.getTitle());
                    return geminiAnalysis;
                }
            } catch (Exception e) {
                log.warn("Gemini job deep-dive failed, falling back to heuristic evaluation: {}", e.getMessage());
            }
        }
        return generateHeuristicJobDeepDive(profile, job);
    }

    private Map<String, Object> callGeminiForJobDeepDive(UserProfile profile, JobOpportunity job) {
        String url = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                geminiModel, geminiApiKey);

        String prompt = String.format("""
                You are an elite International Tech Recruiter & Immigration Career Strategist.
                Analyze the fit between this Candidate and this specific Job Opportunity.
                
                Candidate:
                - Name: %s
                - Title: %s
                - Experience: %.1f years
                - Skills: %s
                - Target Roles: %s
                - Willing to Relocate: %s
                - Languages: %s
                
                Job Opportunity:
                - Title: %s
                - Company: %s
                - Location: %s (%s)
                - Work Type: %s
                - Salary: %s
                - Visa Sponsorship: %s
                - Required Skills: %s
                - Description: %s
                
                Output STRICT JSON in English with this exact structure:
                {
                  "aiSummary": "Comprehensive 3-4 sentence analysis explaining why the candidate matches or where the critical gaps are, specifically comparing their skills with the role requirements.",
                  "visaSuitability": "Realistic immigration/visa/remote legal feasibility analysis for this candidate moving to or working for a company in %s.",
                  "actionItems": [
                    "Tactical step 1 to immediately boost shortlisting odds for this role",
                    "Tactical step 2 (project, demo, or resume keyword)",
                    "Tactical step 3 (interview strategy or networking)"
                  ],
                  "interviewQuestions": [
                    "Likely technical or architecture interview question for this role",
                    "Likely behavioral/STAR question based on the job scope",
                    "Domain-specific scenario question"
                  ],
                  "interviewTips": "A golden tip for standing out during the technical interview round for this specific company."
                }
                """,
                profile.getFullName(), profile.getCurrentTitle(),
                profile.getYearsOfExperience() != null ? profile.getYearsOfExperience() : 1.0,
                profile.getSkills(), profile.getTargetRoles(),
                Boolean.TRUE.equals(profile.getWillingToRelocate()) ? "Yes" : "No",
                profile.getLanguages(),
                job.getTitle(), job.getCompany(), job.getLocation(), job.getCountry(),
                job.getWorkType(), job.getSalaryRange(),
                Boolean.TRUE.equals(job.getVisaSponsorship()) ? "Yes" : "No",
                job.getRequiredSkills(),
                job.getDescription() != null && job.getDescription().length() > 600 ? job.getDescription().substring(0, 600) : job.getDescription(),
                job.getCountry()
        );

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                ),
                "generationConfig", Map.of(
                        "response_mime_type", "application/json"
                )
        );

        RestClient client = RestClient.create();
        String jsonResult = client.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(jsonResult);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                String text = candidates.get(0).path("content").path("parts").get(0).path("text").asText();
                return objectMapper.readValue(text, Map.class);
            }
        } catch (Exception e) {
            log.error("Failed to parse Gemini deep-dive JSON: {}", e.getMessage());
        }
        return null;
    }

    private Map<String, Object> generateHeuristicJobDeepDive(UserProfile profile, JobOpportunity job) {
        Map<String, Object> res = new HashMap<>();
        res.put("aiSummary", String.format("Candidate %s possesses strong technical skills aligning with %s at %s. Focus on articulating hands-on production impact.",
                profile.getFullName(), job.getTitle(), job.getCompany()));
        if (Boolean.TRUE.equals(job.getIsOverseas())) {
            if ("REMOTE".equalsIgnoreCase(job.getWorkType())) {
                res.put("visaSuitability", "100% Worldwide Remote position — Work from home with no immigration visa needed.");
            } else if (Boolean.TRUE.equals(job.getVisaSponsorship())) {
                res.put("visaSuitability", "Company offers work permit / visa sponsorship for qualified international candidates in " + job.getCountry() + ".");
            } else {
                res.put("visaSuitability", "Overseas role requiring local work authorization in " + job.getCountry() + ".");
            }
        } else {
            res.put("visaSuitability", "Domestic position in Vietnam — standard local employment terms.");
        }
        res.put("actionItems", List.of(
                "Tailor resume bullet points to include exact technical keywords from the job specification.",
                "Prepare a GitHub repository demonstration showcasing microservices architecture and automated tests.",
                "Review system design trade-offs relevant to " + job.getTitle() + "."
        ));
        res.put("interviewQuestions", List.of(
                "How would you design and optimize a scalable backend service handling high concurrent traffic?",
                "Describe a challenging technical problem you solved using " + (profile.getSkillList().isEmpty() ? "your core stack" : profile.getSkillList().get(0)) + ".",
                "How do you handle disagreement with team members over architectural choices?"
        ));
        res.put("interviewTips", "Emphasize measurable business impact (reduced latency, cost savings, high availability) rather than merely listing technologies used.");
        return res;
    }

    private String generateHeuristicCoachResponse(UserProfile profile, String userPrompt) {
        String lower = userPrompt.toLowerCase();
        String candidateTitle = profile.getCurrentTitle() != null ? profile.getCurrentTitle() : "Software Engineer";
        List<String> skills = profile.getSkillList();

        if (lower.contains("singapore") || lower.contains("overseas") || lower.contains("visa") || lower.contains("japan") || lower.contains("germany")) {
            return String.format(
                    "### 🌏 Overseas Tech Career & Visa Sponsorship Strategy for %s\n\n" +
                    "Hello! When targeting international markets (Singapore, Germany, Japan, Australia, US Remote), global recruiters evaluate 3 critical pillars:\n\n" +
                    "1. **Visa Sponsorship Requirements**:\n" +
                    "   - **Singapore**: Minimum qualifying monthly salary for Employment Pass (EP) is 5,000 SGD + passing COMPASS criteria.\n" +
                    "   - **Germany / EU**: EU Blue Card requires a recognized university degree (Anabin H+) and minimum gross salary ~45,300 EUR/year for IT shortages.\n" +
                    "   - **Japan**: Highly Skilled Professional (HSP) visa provides fast-tracked permanent residency for software engineers.\n\n" +
                    "2. **Global 1-Page Resume Standards**:\n" +
                    "   - Remove photo, marital status, and exact date of birth.\n" +
                    "   - Every bullet must feature **Metrics & Business Impact** (*e.g., 'Optimized database queries with Redis, improving response time by 40%%'*).\n\n" +
                    "3. **Effective Application Channels**:\n" +
                    "   - Target listings with `Visa Sponsorship` or `Relocation Support` on LinkedIn Jobs, Otta, Relocate.me, and Wellfound.\n\n" +
                    "Which specific country market would you like to explore deeper?", candidateTitle);
        } else if (lower.contains("interview") || lower.contains("mock") || lower.contains("questions")) {
            return String.format(
                    "### 🎯 Tech Interview Mastery for %s\n\n" +
                    "In international tech interviews, hiring loops generally consist of 3 stages:\n\n" +
                    "1. **Behavioral Round (STAR Methodology)**:\n" +
                    "   - *Classic prompt*: 'Tell me about a time you had a technical disagreement with a teammate and how you resolved it.'\n" +
                    "   - *Formula*: **S**ituation -> **T**ask -> **A**ction -> **R**esult with quantifiable impact.\n\n" +
                    "2. **Technical Deep-Dive (%s)**:\n" +
                    "   - Interviewers assess architecture decisions: *Spring Boot Bean Lifecycle*, *Concurrency & Locking in MySQL*, *Distributed Cache Invalidation Strategies*.\n\n" +
                    "3. **System Design**:\n" +
                    "   - High-level architecture: Rate Limiters, Distributed Task Queues, Real-time WebSocket Messaging.\n\n" +
                    "Would you like to start a simulated **Mock Interview** right now? Just type 'Start Mock Interview'!",
                    candidateTitle, skills.isEmpty() ? "Backend & Cloud" : String.join(", ", skills.subList(0, Math.min(3, skills.size()))));
        } else {
            return String.format(
                    "Hello %s! I am your **AI Career Coach** 🚀\n\n" +
                    "Based on your profile as a **%s** with %.1f years of experience, here is how I can assist you:\n\n" +
                    "- 🔍 **Job Matching & Skill Gap Analysis**: Discover gaps preventing you from securing high-paying global remote or overseas positions.\n" +
                    "- ✈️ **Visa & Relocation Strategy**: Navigate work visas (Singapore EP, Germany EU Blue Card, Japan HSP) and employer sponsorships.\n" +
                    "- 📝 **ATS Resume Optimization**: Rewrite bullet points using the STAR formula with quantifiable metrics.\n" +
                    "- 🎤 **Technical Mock Interviews**: Conduct realistic interview simulations in English.\n\n" +
                    "What would you like to focus on today?",
                    profile.getFullName(), candidateTitle, profile.getYearsOfExperience() != null ? profile.getYearsOfExperience() : 1.0);
        }
    }
}
