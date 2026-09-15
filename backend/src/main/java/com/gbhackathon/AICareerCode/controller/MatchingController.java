package com.gbhackathon.AICareerCode.controller;

import com.gbhackathon.AICareerCode.dto.MatchResultDto;
import com.gbhackathon.AICareerCode.model.JobOpportunity;
import com.gbhackathon.AICareerCode.model.UserProfile;
import com.gbhackathon.AICareerCode.service.AiCoachService;
import com.gbhackathon.AICareerCode.service.JobMatchingService;
import com.gbhackathon.AICareerCode.service.JobService;
import com.gbhackathon.AICareerCode.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = "*")
public class MatchingController {

    private final ProfileService profileService;
    private final JobService jobService;
    private final JobMatchingService matchingService;
    private final AiCoachService aiCoachService;

    public MatchingController(ProfileService profileService, JobService jobService, JobMatchingService matchingService, AiCoachService aiCoachService) {
        this.profileService = profileService;
        this.jobService = jobService;
        this.matchingService = matchingService;
        this.aiCoachService = aiCoachService;
    }

    @GetMapping
    public ResponseEntity<List<MatchResultDto>> getMatchesForCurrentProfile(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isOverseas,
            @RequestParam(required = false) String workType,
            @RequestParam(required = false) Boolean visaSponsorship
    ) {
        UserProfile profile = profileService.getCurrentOrCreateProfile();
        List<JobOpportunity> jobs = jobService.searchJobs(keyword, isOverseas, workType, visaSponsorship);
        List<MatchResultDto> matches = matchingService.matchJobsForProfile(profile, jobs);
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<MatchResultDto> getMatchForSpecificJob(@PathVariable Long jobId) {
        UserProfile profile = profileService.getCurrentOrCreateProfile();
        return jobService.getJobById(jobId)
                .map(job -> ResponseEntity.ok(matchingService.evaluateMatch(profile, job)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{jobId}/ai-deep-dive")
    public ResponseEntity<Map<String, Object>> getAiDeepDiveForJob(@PathVariable Long jobId) {
        UserProfile profile = profileService.getCurrentOrCreateProfile();
        return jobService.getJobById(jobId)
                .map(job -> ResponseEntity.ok(aiCoachService.generateJobDeepDive(profile, job)))
                .orElse(ResponseEntity.notFound().build());
    }
}
