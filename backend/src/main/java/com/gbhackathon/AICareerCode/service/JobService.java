package com.gbhackathon.AICareerCode.service;

import com.gbhackathon.AICareerCode.dto.JobDto;
import com.gbhackathon.AICareerCode.model.JobOpportunity;
import com.gbhackathon.AICareerCode.repository.JobOpportunityRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JobService {

    private final JobOpportunityRepository jobRepository;

    public JobService(JobOpportunityRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public List<JobOpportunity> getAllJobs() {
        return jobRepository.findAll();
    }

    public Optional<JobOpportunity> getJobById(Long id) {
        return jobRepository.findById(id);
    }

    public List<JobOpportunity> searchJobs(String keyword, Boolean isOverseas, String workType, Boolean visaSponsorship) {
        String cleanKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String cleanWorkType = (workType != null && !workType.isBlank() && !workType.equalsIgnoreCase("ALL")) ? workType : null;
        return jobRepository.searchJobs(cleanKeyword, isOverseas, cleanWorkType, visaSponsorship);
    }

    public JobDto toDto(JobOpportunity job) {
        JobDto dto = new JobDto();
        dto.setId(job.getId());
        dto.setTitle(job.getTitle());
        dto.setCompany(job.getCompany());
        dto.setCompanyLogo(job.getCompanyLogo());
        dto.setLocation(job.getLocation());
        dto.setCountry(job.getCountry());
        dto.setIsOverseas(job.getIsOverseas());
        dto.setWorkType(job.getWorkType());
        dto.setSalaryRange(job.getSalaryRange());
        dto.setExperienceLevel(job.getExperienceLevel());
        dto.setMinYearsExp(job.getMinYearsExp());
        dto.setRequiredSkills(job.getRequiredSkillList());
        dto.setPreferredSkills(job.getPreferredSkillList());
        dto.setVisaSponsorship(job.getVisaSponsorship());
        dto.setRelocationAssistance(job.getRelocationAssistance());
        dto.setLanguageRequirements(job.getLanguageRequirements());
        dto.setDescription(job.getDescription());
        dto.setRequirements(job.getRequirements());
        dto.setBenefits(job.getBenefits());
        dto.setApplyUrl(job.getApplyUrl());
        dto.setSource(job.getSource());
        dto.setPostedAt(job.getPostedAt());
        return dto;
    }

    public List<JobDto> toDtoList(List<JobOpportunity> list) {
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }
}
