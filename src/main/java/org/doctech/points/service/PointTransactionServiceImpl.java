package org.doctech.points.service;

import lombok.RequiredArgsConstructor;
import org.doctech.common.exception.InsufficientPointsException;
import org.doctech.common.exception.TransactionNotFoundException;
import org.doctech.common.exception.UserNotFoundException;
import org.doctech.common.utils.ValidationUtils;
import org.doctech.points.dto.PointTransactionDTO;
import org.doctech.points.mapper.PointTransactionMapper;
import org.doctech.points.model.PointTransaction;
import org.doctech.points.model.TransactionType;
import org.doctech.points.repository.PointTransactionRepository;
import org.doctech.user.model.User;
import org.doctech.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PointTransactionServiceImpl implements PointTransactionService {

    private final PointTransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final PointTransactionMapper transactionMapper;

    @Override
    public PointTransactionDTO createTransaction(UUID userId, Integer points, TransactionType type, String description) {
        User user = findAndValidateUser(userId);
        validatePointsTransaction(userId, points);

        PointTransaction transaction = PointTransaction.builder()
                .user(user)
                .points(points)
                .type(type)
                .description(description)
                .successful(true)
                .build();

        PointTransaction savedTransaction = transactionRepository.save(transaction);
        return transactionMapper.toDTO(savedTransaction);
    }

    @Override
    public void spendPoints(UUID userId, Integer points, TransactionType type, String description) {
        validatePoints(userId, points);
        createTransaction(userId, -points, type, description);
    }

    @Override
    public void validatePoints(UUID userId, Integer requiredPoints) {
        Integer currentBalance = getUserBalance(userId);
        if (currentBalance < requiredPoints) {
            throw new InsufficientPointsException(
                    String.format("Insufficient points. Required: %d, Available: %d", requiredPoints, currentBalance));
        }
    }

    @Override
    public PointTransactionDTO createTransaction(PointTransactionDTO transactionDTO) {
        ValidationUtils.validate(transactionDTO);
        User user = findAndValidateUser(transactionDTO.getUserId());
        validatePointsTransaction(user.getId(), transactionDTO.getPoints());

        PointTransaction transaction = transactionMapper.toEntity(transactionDTO);
        transaction.setUser(user);

        PointTransaction savedTransaction = transactionRepository.save(transaction);
        return transactionMapper.toDTO(savedTransaction);
    }

    @Override
    public PointTransactionDTO rollbackTransaction(UUID transactionId) {
        PointTransaction originalTransaction = findTransactionById(transactionId);
        validateRollbackEligibility(originalTransaction);

        PointTransaction rollbackTransaction = createRollbackTransaction(originalTransaction);
        markOriginalTransactionAsRolledBack(originalTransaction);

        PointTransaction savedRollback = transactionRepository.save(rollbackTransaction);
        return transactionMapper.toDTO(savedRollback);
    }

    @Override
    @Transactional(readOnly = true)
    public PointTransactionDTO getTransactionById(UUID id) {
        return transactionMapper.toDTO(findTransactionById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PointTransactionDTO> getUserTransactionHistory(UUID userId) {
        validateUserExists(userId);
        return transactionRepository.findByUserIdOrderByTransactionDateDesc(userId).stream()
                .map(transactionMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PointTransactionDTO> getUserTransactions(UUID userId, Pageable pageable) {
        validateUserExists(userId);
        return transactionRepository.findByUserIdOrderByTransactionDateDesc(userId, pageable)
                .map(transactionMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PointTransactionDTO> getUserTransactionsByType(UUID userId, TransactionType type, Pageable pageable) {
        validateUserExists(userId);
        return transactionRepository.findByUserIdAndType(userId, type, pageable)
                .map(transactionMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PointTransactionDTO> getUserTransactionsInDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        validateUserExists(userId);
        return transactionRepository.findUserTransactionsInDateRange(userId, startDate, endDate).stream()
                .map(transactionMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Integer calculateUserBalance(UUID userId) {
        validateUserExists(userId);
        Integer balance = transactionRepository.calculateUserTotalPoints(userId);
        return balance != null ? balance : 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer calculateUserPointsByType(UUID userId, TransactionType type) {
        validateUserExists(userId);
        Integer points = transactionRepository.calculateUserPointsByType(userId, type);
        return points != null ? points : 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getUserBalance(UUID userId) {
        return calculateUserBalance(userId);
    }

    private User findAndValidateUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }

    private void validateUserExists(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }
    }

    private PointTransaction findTransactionById(UUID transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with id: " + transactionId));
    }

    private void validatePointsTransaction(UUID userId, Integer points) {
        if (points < 0) {
            Integer currentBalance = calculateUserBalance(userId);
            if (Math.abs(points) > currentBalance) {
                throw new InsufficientPointsException("Insufficient points for this transaction");
            }
        }
    }

    private void validateRollbackEligibility(PointTransaction transaction) {
        if (!transaction.isSuccessful()) {
            throw new IllegalStateException("Cannot rollback an unsuccessful transaction");
        }
    }

    private PointTransaction createRollbackTransaction(PointTransaction originalTransaction) {
        return PointTransaction.builder()
                .user(originalTransaction.getUser())
                .relatedItemId(originalTransaction.getRelatedItemId())
                .type(originalTransaction.getType())
                .points(-originalTransaction.getPoints())
                .description("Rollback of transaction: " + originalTransaction.getId())
                .rollbackTransactionId(originalTransaction.getId())
                .successful(true)
                .build();
    }

    private void markOriginalTransactionAsRolledBack(PointTransaction transaction) {
        transaction.setSuccessful(false);
        transactionRepository.save(transaction);
    }
}