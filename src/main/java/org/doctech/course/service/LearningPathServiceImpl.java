package org.doctech.course.service;

import lombok.RequiredArgsConstructor;
import org.doctech.common.exception.LearningPathNotFoundException;
import org.doctech.common.utils.ValidationUtils;
import org.doctech.course.dto.LearningPathDTO;
import org.doctech.course.mapper.LearningPathMapper;
import org.doctech.course.model.DifficultyLevel;
import org.doctech.course.model.LearningPath;
import org.doctech.course.repository.LearningPathRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LearningPathServiceImpl implements LearningPathService {

    private final LearningPathRepository learningPathRepository;
    private final LearningPathMapper learningPathMapper;

    @Override
    public LearningPathDTO createLearningPath(LearningPathDTO learningPathDTO) {
        ValidationUtils.validate(learningPathDTO);

        if (learningPathRepository.existsByTitle(learningPathDTO.getTitle())) {
            throw new IllegalStateException("Learning path with title " + learningPathDTO.getTitle() + " already exists");
        }

        LearningPath learningPath = learningPathMapper.toEntity(learningPathDTO);
        LearningPath savedLearningPath = learningPathRepository.save(learningPath);
        return learningPathMapper.toDTO(savedLearningPath);
    }

    @Override
    public LearningPathDTO updateLearningPath(UUID id, LearningPathDTO learningPathDTO) {
        ValidationUtils.validate(learningPathDTO);

        LearningPath existingPath = learningPathRepository.findById(id)
                .orElseThrow(() -> new LearningPathNotFoundException("Learning path not found with id: " + id));

        // Check if the new title is already taken by another path
        if (!existingPath.getTitle().equals(learningPathDTO.getTitle()) &&
                learningPathRepository.existsByTitle(learningPathDTO.getTitle())) {
            throw new IllegalStateException("Learning path with title " + learningPathDTO.getTitle() + " already exists");
        }

        // Update the fields
        existingPath.setTitle(learningPathDTO.getTitle());
        existingPath.setDescription(learningPathDTO.getDescription());
        existingPath.setDifficulty(learningPathDTO.getDifficulty());

        LearningPath updatedPath = learningPathRepository.save(existingPath);
        return learningPathMapper.toDTO(updatedPath);
    }

    @Override
    @Transactional(readOnly = true)
    public LearningPathDTO getLearningPathById(UUID id) {
        LearningPath learningPath = learningPathRepository.findByIdWithCourses(id)
                .orElseThrow(() -> new LearningPathNotFoundException("Learning path not found with id: " + id));
        return learningPathMapper.toDTO(learningPath);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LearningPathDTO> getAllLearningPaths(Pageable pageable) {
        return learningPathRepository.findAll(pageable)
                .map(learningPathMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LearningPathDTO> getLearningPathsByDifficulty(DifficultyLevel difficulty, Pageable pageable) {
        return learningPathRepository.findByDifficulty(difficulty, pageable)
                .map(learningPathMapper::toDTO);
    }

    @Override
    public void deleteLearningPath(UUID id) {
        if (!learningPathRepository.existsById(id)) {
            throw new LearningPathNotFoundException("Learning path not found with id: " + id);
        }
        learningPathRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningPathDTO> getAvailablePaths(Integer maxPoints) {
        ValidationUtils.validateNotNull(maxPoints, "Max points cannot be null");
        return learningPathRepository.findAvailablePaths(maxPoints).stream()
                .map(learningPathMapper::toDTO)
                .toList();
    }
}