package com.gbhackathon.AICareerCode.dto;

import java.util.List;

public class CareerRoadmapDto {
    private String targetGoal;
    private List<RoadmapMilestone> months3;
    private List<RoadmapMilestone> months6;
    private List<RoadmapMilestone> months12;

    public static class RoadmapMilestone {
        private String title;
        private String description;
        private String category; // "SKILL", "CERTIFICATION", "PROJECT", "NETWORKING"
        private String estimatedHours;

        public RoadmapMilestone() {}

        public RoadmapMilestone(String title, String description, String category, String estimatedHours) {
            this.title = title;
            this.description = description;
            this.category = category;
            this.estimatedHours = estimatedHours;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getEstimatedHours() {
            return estimatedHours;
        }

        public void setEstimatedHours(String estimatedHours) {
            this.estimatedHours = estimatedHours;
        }
    }

    public String getTargetGoal() {
        return targetGoal;
    }

    public void setTargetGoal(String targetGoal) {
        this.targetGoal = targetGoal;
    }

    public List<RoadmapMilestone> getMonths3() {
        return months3;
    }

    public void setMonths3(List<RoadmapMilestone> months3) {
        this.months3 = months3;
    }

    public List<RoadmapMilestone> getMonths6() {
        return months6;
    }

    public void setMonths6(List<RoadmapMilestone> months6) {
        this.months6 = months6;
    }

    public List<RoadmapMilestone> getMonths12() {
        return months12;
    }

    public void setMonths12(List<RoadmapMilestone> months12) {
        this.months12 = months12;
    }
}
