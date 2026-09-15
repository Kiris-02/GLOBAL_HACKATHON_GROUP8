package com.gbhackathon.AICareerCode.dto;

import java.util.List;

public class ResumeAuditDto {
    private int healthScore; // 0 - 100
    private String verdict;   // "Cần cải thiện", "Khá tốt", "Xuất sắc chuẩn quốc tế"
    private String summary;
    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> atsKeywordsPresent;
    private List<String> atsKeywordsMissing;
    private List<BulletImprovement> bulletImprovements;
    private CareerRoadmapDto careerRoadmap;

    public static class BulletImprovement {
        private String original;
        private String improved;
        private String rationale;

        public BulletImprovement() {}

        public BulletImprovement(String original, String improved, String rationale) {
            this.original = original;
            this.improved = improved;
            this.rationale = rationale;
        }

        public String getOriginal() {
            return original;
        }

        public void setOriginal(String original) {
            this.original = original;
        }

        public String getImproved() {
            return improved;
        }

        public void setImproved(String improved) {
            this.improved = improved;
        }

        public String getRationale() {
            return rationale;
        }

        public void setRationale(String rationale) {
            this.rationale = rationale;
        }
    }

    public int getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(int healthScore) {
        this.healthScore = healthScore;
    }

    public String getVerdict() {
        return verdict;
    }

    public void setVerdict(String verdict) {
        this.verdict = verdict;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getStrengths() {
        return strengths;
    }

    public void setStrengths(List<String> strengths) {
        this.strengths = strengths;
    }

    public List<String> getWeaknesses() {
        return weaknesses;
    }

    public void setWeaknesses(List<String> weaknesses) {
        this.weaknesses = weaknesses;
    }

    public List<String> getAtsKeywordsPresent() {
        return atsKeywordsPresent;
    }

    public void setAtsKeywordsPresent(List<String> atsKeywordsPresent) {
        this.atsKeywordsPresent = atsKeywordsPresent;
    }

    public List<String> getAtsKeywordsMissing() {
        return atsKeywordsMissing;
    }

    public void setAtsKeywordsMissing(List<String> atsKeywordsMissing) {
        this.atsKeywordsMissing = atsKeywordsMissing;
    }

    public List<BulletImprovement> getBulletImprovements() {
        return bulletImprovements;
    }

    public void setBulletImprovements(List<BulletImprovement> bulletImprovements) {
        this.bulletImprovements = bulletImprovements;
    }

    public CareerRoadmapDto getCareerRoadmap() {
        return careerRoadmap;
    }

    public void setCareerRoadmap(CareerRoadmapDto careerRoadmap) {
        this.careerRoadmap = careerRoadmap;
    }
}
