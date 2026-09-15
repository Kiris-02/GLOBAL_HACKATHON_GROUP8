package com.gbhackathon.AICareerCode.controller;

import com.gbhackathon.AICareerCode.dto.CareerRoadmapDto;
import com.gbhackathon.AICareerCode.dto.ChatMessageDto;
import com.gbhackathon.AICareerCode.dto.ResumeAuditDto;
import com.gbhackathon.AICareerCode.model.UserProfile;
import com.gbhackathon.AICareerCode.service.AiCoachService;
import com.gbhackathon.AICareerCode.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coach")
@CrossOrigin(origins = "*")
public class CoachController {

    private final ProfileService profileService;
    private final AiCoachService aiCoachService;

    public CoachController(ProfileService profileService, AiCoachService aiCoachService) {
        this.profileService = profileService;
        this.aiCoachService = aiCoachService;
    }

    @GetMapping("/audit")
    public ResponseEntity<ResumeAuditDto> getProfileAudit() {
        UserProfile profile = profileService.getCurrentOrCreateProfile();
        ResumeAuditDto audit = aiCoachService.auditProfile(profile);
        return ResponseEntity.ok(audit);
    }

    @GetMapping("/roadmap")
    public ResponseEntity<CareerRoadmapDto> getCareerRoadmap() {
        UserProfile profile = profileService.getCurrentOrCreateProfile();
        CareerRoadmapDto roadmap = aiCoachService.generateCareerRoadmap(profile);
        return ResponseEntity.ok(roadmap);
    }

    public static class ChatRequest {
        private String message;
        private List<ChatMessageDto> history;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public List<ChatMessageDto> getHistory() {
            return history;
        }

        public void setHistory(List<ChatMessageDto> history) {
            this.history = history;
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chatWithCoach(@RequestBody ChatRequest request) {
        UserProfile profile = profileService.getCurrentOrCreateProfile();
        String userMsg = request.getMessage() != null ? request.getMessage().trim() : "";
        if (userMsg.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("reply", "Vui lòng nhập câu hỏi của bạn."));
        }
        String reply = aiCoachService.chat(profile, request.getHistory(), userMsg);
        return ResponseEntity.ok(Map.of("reply", reply));
    }
}
