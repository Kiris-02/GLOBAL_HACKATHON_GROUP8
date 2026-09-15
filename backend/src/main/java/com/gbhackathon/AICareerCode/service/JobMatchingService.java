package com.gbhackathon.AICareerCode.service;

import com.gbhackathon.AICareerCode.dto.JobDto;
import com.gbhackathon.AICareerCode.dto.MatchResultDto;
import com.gbhackathon.AICareerCode.model.JobOpportunity;
import com.gbhackathon.AICareerCode.model.UserProfile;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class JobMatchingService {

    private final JobService jobService;

    public JobMatchingService(JobService jobService) {
        this.jobService = jobService;
    }

    public List<MatchResultDto> matchJobsForProfile(UserProfile profile, List<JobOpportunity> jobs) {
        return jobs.stream()
                .map(job -> evaluateMatch(profile, job))
                .sorted((a, b) -> Integer.compare(b.getOverallScore(), a.getOverallScore()))
                .collect(Collectors.toList());
    }

    public MatchResultDto evaluateMatch(UserProfile profile, JobOpportunity job) {
        MatchResultDto result = new MatchResultDto();
        result.setJob(jobService.toDto(job));

        List<String> userSkills = profile.getSkillList();
        List<String> reqSkills = job.getRequiredSkillList();
        List<String> prefSkills = job.getPreferredSkillList();

        // 1. Match Skills
        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String req : reqSkills) {
            if (containsSkill(userSkills, req)) {
                matched.add(req);
            } else {
                missing.add(req);
            }
        }

        for (String pref : prefSkills) {
            if (containsSkill(userSkills, pref)) {
                if (!matched.contains(pref)) matched.add(pref);
            } else {
                if (!missing.contains(pref)) missing.add(pref);
            }
        }

        int reqMatchedCount = (int) reqSkills.stream().filter(r -> containsSkill(userSkills, r)).count();
        double reqRatio = reqSkills.isEmpty() ? 1.0 : (double) reqMatchedCount / reqSkills.size();

        int prefMatchedCount = (int) prefSkills.stream().filter(p -> containsSkill(userSkills, p)).count();
        double prefRatio = prefSkills.isEmpty() ? 1.0 : (double) prefMatchedCount / prefSkills.size();

        int skillScore = (int) Math.min(100, Math.round((reqRatio * 75.0) + (prefRatio * 25.0)));
        result.setSkillsScore(skillScore);
        result.setMatchedSkills(matched);
        result.setMissingSkills(missing);

        // 2. Experience Score
        double userExp = profile.getYearsOfExperience() != null ? profile.getYearsOfExperience() : 1.0;
        int jobMinExp = job.getMinYearsExp() != null ? job.getMinYearsExp() : 0;
        int expScore;
        if (userExp >= jobMinExp) {
            expScore = 100;
        } else if (userExp >= jobMinExp - 1) {
            expScore = 80;
        } else if (userExp >= jobMinExp - 2) {
            expScore = 60;
        } else {
            expScore = 40;
        }
        result.setExperienceScore(expScore);

        // 3. Relocation & Overseas Score
        int relocationScore = 85;
        String visaSuitability;

        if (Boolean.TRUE.equals(job.getIsOverseas())) {
            boolean userWilling = Boolean.TRUE.equals(profile.getWillingToRelocate());
            boolean isRemoteJob = "REMOTE".equalsIgnoreCase(job.getWorkType());
            boolean hasVisaSupport = Boolean.TRUE.equals(job.getVisaSponsorship());

            if (isRemoteJob) {
                relocationScore = 95;
                visaSuitability = "100% Worldwide Remote position — Work from home with no immigration visa needed.";
            } else if (userWilling && hasVisaSupport) {
                relocationScore = 90;
                visaSuitability = "Company provides full Employment Visa Sponsorship & Relocation Allowance.";
            } else if (userWilling) {
                relocationScore = 75;
                visaSuitability = "Verify work visa sponsorship availability and immigration requirements for " + job.getCountry() + ".";
            } else {
                relocationScore = 45;
                visaSuitability = "Onsite overseas position — evaluate immigration eligibility and relocation feasibility.";
            }
        } else {
            relocationScore = 95;
            visaSuitability = "Domestic role (Vietnam) — fully compliant with local labor regulations and language.";
        }
        result.setRelocationScore(relocationScore);
        result.setVisaSuitability(visaSuitability);

        // 4. Overall Score calculation
        int overallScore = (int) Math.round((skillScore * 0.55) + (expScore * 0.25) + (relocationScore * 0.20));
        overallScore = Math.max(10, Math.min(99, overallScore));
        result.setOverallScore(overallScore);

        // 5. AI Summary & Action items
        String summary;
        if (overallScore >= 80) {
            summary = String.format("You are a high-potential candidate for %s at %s. You match %d/%d core skills. Apply with confidence!",
                    job.getTitle(), job.getCompany(), matched.size(), (matched.size() + missing.size()));
        } else if (overallScore >= 60) {
            summary = String.format("Your profile matches %d%% of requirements for %s. Upskilling in %s will significantly increase your interview shortlist chances.",
                    overallScore, job.getCompany(), missing.isEmpty() ? "specialized areas" : String.join(", ", missing.subList(0, Math.min(2, missing.size()))));
        } else {
            summary = String.format("This role demands deeper experience in: %s. Target this as a key milestone after completing your 6-month roadmap.",
                    missing.isEmpty() ? "advanced architecture" : String.join(", ", missing.subList(0, Math.min(3, missing.size()))));
        }
        result.setAiSummary(summary);

        List<String> actions = new ArrayList<>();
        if (!missing.isEmpty()) {
            actions.add("Incorporate keywords and project demos covering: " + String.join(", ", missing.subList(0, Math.min(2, missing.size()))));
        }
        if (Boolean.TRUE.equals(job.getIsOverseas()) && !"REMOTE".equalsIgnoreCase(job.getWorkType())) {
            actions.add("Prepare an international-standard 1-page English resume and required language credentials for " + job.getCountry());
        }
        actions.add("Rehearse STAR-format behavioral interview stories highlighting: " + String.join(", ", matched.subList(0, Math.min(3, matched.size()))));
        result.setActionItems(actions);

        return result;
    }

    private boolean containsSkill(List<String> userSkills, String targetSkill) {
        if (userSkills == null || targetSkill == null) return false;
        String target = targetSkill.trim().toLowerCase();
        for (String s : userSkills) {
            String cand = s.trim().toLowerCase();
            if (cand.equals(target) || cand.contains(target) || target.contains(cand)) {
                return true;
            }
            // Synonyms
            if (target.contains("spring") && cand.contains("spring")) return true;
            if (target.contains("react") && cand.contains("react")) return true;
            if (target.contains("sql") && (cand.contains("mysql") || cand.contains("postgres"))) return true;
            if (target.contains("k8s") && cand.contains("kubernetes")) return true;
            if (target.contains("kubernetes") && cand.contains("k8s")) return true;
            if (target.contains("aws") && cand.contains("cloud")) return true;
        }
        return false;
    }
}
