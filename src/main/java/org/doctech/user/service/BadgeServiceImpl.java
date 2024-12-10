package org.doctech.user.service;

import lombok.RequiredArgsConstructor;
import org.doctech.common.exception.BadgeAlreadyExistsException;
import org.doctech.common.exception.BadgeNotFoundException;
import org.doctech.common.utils.ValidationUtils;
import org.doctech.user.dto.BadgeDTO;
import org.doctech.user.mapper.BadgeMapper;
import org.doctech.user.model.Badge;
import org.doctech.user.model.BadgeType;
import org.doctech.user.repository.BadgeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BadgeServiceImpl implements BadgeService {

    private final BadgeRepository badgeRepository;
    private final BadgeMapper badgeMapper;

    @Override
    public BadgeDTO createBadge(BadgeDTO badgeDTO) {
        ValidationUtils.validate(badgeDTO);

        if (badgeRepository.existsByName(badgeDTO.getName())) {
            throw new BadgeAlreadyExistsException("Badge with name " + badgeDTO.getName() + " already exists");
        }

        Badge badge = badgeMapper.toEntity(badgeDTO);
        Badge savedBadge = badgeRepository.save(badge);
        return badgeMapper.toDTO(savedBadge);
    }

    @Override
    public BadgeDTO updateBadge(UUID id, BadgeDTO badgeDTO) {
        ValidationUtils.validate(badgeDTO);

        Badge existingBadge = badgeRepository.findById(id)
                .orElseThrow(() -> new BadgeNotFoundException("Badge not found with id: " + id));

        if (!existingBadge.getName().equals(badgeDTO.getName()) &&
                badgeRepository.existsByName(badgeDTO.getName())) {
            throw new BadgeAlreadyExistsException("Badge with name " + badgeDTO.getName() + " already exists");
        }

        existingBadge.setName(badgeDTO.getName());
        existingBadge.setDescription(badgeDTO.getDescription());
        existingBadge.setImageUrl(badgeDTO.getImageUrl());
        existingBadge.setType(badgeDTO.getType());
        existingBadge.setPointsRequired(badgeDTO.getPointsRequired());
        existingBadge.setPointsCost(badgeDTO.getPointsCost());

        Badge updatedBadge = badgeRepository.save(existingBadge);
        return badgeMapper.toDTO(updatedBadge);
    }

    @Override
    @Transactional(readOnly = true)
    public BadgeDTO getBadgeById(UUID id) {
        Badge badge = badgeRepository.findById(id)
                .orElseThrow(() -> new BadgeNotFoundException("Badge not found with id: " + id));
        return badgeMapper.toDTO(badge);
    }

    @Override
    @Transactional(readOnly = true)
    public BadgeDTO getBadgeByName(String name) {
        Badge badge = badgeRepository.findByName(name)
                .orElseThrow(() -> new BadgeNotFoundException("Badge not found with name: " + name));
        return badgeMapper.toDTO(badge);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BadgeDTO> getBadgesByType(BadgeType type) {
        return badgeRepository.findByType(type).stream()
                .map(badgeMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BadgeDTO> getAllBadges(Pageable pageable) {
        return badgeRepository.findAll(pageable)
                .map(badgeMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BadgeDTO> getAvailableBadges(Integer points) {
        return badgeRepository.findAvailableBadges(points).stream()
                .map(badgeMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BadgeDTO> getPurchaseableBadges(Integer points) {
        return badgeRepository.findPurchaseableBadges(points).stream()
                .map(badgeMapper::toDTO)
                .toList();
    }

    @Override
    public void deleteBadge(UUID id) {
        if (!badgeRepository.existsById(id)) {
            throw new BadgeNotFoundException("Badge not found with id: " + id);
        }
        badgeRepository.deleteById(id);
    }
}