package org.doctech.user.service;

import lombok.RequiredArgsConstructor;
import org.doctech.common.exception.AchievementAlreadyExistsException;
import org.doctech.common.exception.AchievementNotFoundException;
import org.doctech.common.utils.ValidationUtils;
import org.doctech.user.dto.AchievementDTO;
import org.doctech.user.mapper.AchievementMapper;
import org.doctech.user.model.Achievement;
import org.doctech.user.model.AchievementType;
import org.doctech.user.repository.AchievementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AchievementServiceImpl implements AchievementService {

    private final AchievementRepository achievementRepository;
    private final AchievementMapper achievementMapper;

    @Override
    public AchievementDTO createAchievement(AchievementDTO achievementDTO) {
        ValidationUtils.validate(achievementDTO);

        if (achievementRepository.existsByTitle(achievementDTO.getTitle())) {
            throw new AchievementAlreadyExistsException("Achievement with title " + achievementDTO.getTitle() + " already exists");
        }

        Achievement achievement = achievementMapper.toEntity(achievementDTO);
        Achievement savedAchievement = achievementRepository.save(achievement);
        return achievementMapper.toDTO(savedAchievement);
    }

    @Override
    public AchievementDTO updateAchievement(UUID id, AchievementDTO achievementDTO) {
        ValidationUtils.validate(achievementDTO);

        Achievement existingAchievement = achievementRepository.findById(id)
                .orElseThrow(() -> new AchievementNotFoundException("Achievement not found with id: " + id));

        if (!existingAchievement.getTitle().equals(achievementDTO.getTitle()) &&
                achievementRepository.existsByTitle(achievementDTO.getTitle())) {
            throw new AchievementAlreadyExistsException("Achievement with title " + achievementDTO.getTitle() + " already exists");
        }

        existingAchievement.setTitle(achievementDTO.getTitle());
        existingAchievement.setDescription(achievementDTO.getDescription());
        existingAchievement.setPointsReward(achievementDTO.getPointsReward());
        existingAchievement.setType(achievementDTO.getType());
        existingAchievement.setRequiredProgress(achievementDTO.getRequiredProgress());
        existingAchievement.setImageUrl(achievementDTO.getImageUrl());

        Achievement updatedAchievement = achievementRepository.save(existingAchievement);
        return achievementMapper.toDTO(updatedAchievement);
    }

    @Override
    @Transactional(readOnly = true)
    public AchievementDTO getAchievementById(UUID id) {
        Achievement achievement = achievementRepository.findById(id)
                .orElseThrow(() -> new AchievementNotFoundException("Achievement not found with id: " + id));
        return achievementMapper.toDTO(achievement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AchievementDTO> getAchievementsByType(AchievementType type) {
        return achievementRepository.findByType(type).stream()
                .map(achievementMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AchievementDTO> getAllAchievements(Pageable pageable) {
        return achievementRepository.findAll(pageable)
                .map(achievementMapper::toDTO);
    }

    @Override
    public void deleteAchievement(UUID id) {
        if (!achievementRepository.existsById(id)) {
            throw new AchievementNotFoundException("Achievement not found with id: " + id);
        }
        achievementRepository.deleteById(id);
    }
}