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
    public PointTransactionDTO createTransaction(PointTransactionDTO transactionDTO) {
        ValidationUtils.validate(transactionDTO);

        User user = userRepository.findById(transactionDTO.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + transactionDTO.getUserId()));

        Integer currentBalance = calculateUserBalance(user.getId());
        if (transactionDTO.getPoints() < 0 && Math.abs(transactionDTO.getPoints()) > currentBalance) {
            throw new InsufficientPointsException("Insufficient points for this transaction");
        }

        PointTransaction transaction = transactionMapper.toEntity(transactionDTO);
        transaction.setUser(user);

        PointTransaction savedTransaction = transactionRepository.save(transaction);
        return transactionMapper.toDTO(savedTransaction);
    }

    @Override
    public PointTransactionDTO rollbackTransaction(UUID transactionId) {
        PointTransaction originalTransaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with id: " + transactionId));

        if (!originalTransaction.isSuccessful()) {
            throw new IllegalStateException("Cannot rollback an unsuccessful transaction");
        }

        PointTransaction rollbackTransaction = PointTransaction.builder()
                .user(originalTransaction.getUser())
                .relatedItemId(originalTransaction.getRelatedItemId())
                .type(originalTransaction.getType())
                .points(-originalTransaction.getPoints())
                .description("Rollback of transaction: " + transactionId)
                .rollbackTransactionId(originalTransaction.getId())
                .build();

        originalTransaction.setSuccessful(false);
        transactionRepository.save(originalTransaction);

        PointTransaction savedRollback = transactionRepository.save(rollbackTransaction);
        return transactionMapper.toDTO(savedRollback);
    }

    @Override
    @Transactional(readOnly = true)
    public PointTransactionDTO getTransactionById(UUID id) {
        PointTransaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with id: " + id));
        return transactionMapper.toDTO(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PointTransactionDTO> getUserTransactionHistory(UUID userId) {
        validateUser(userId);
        return transactionRepository.findByUserIdOrderByTransactionDateDesc(userId).stream()
                .map(transactionMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PointTransactionDTO> getUserTransactionsByType(UUID userId, TransactionType type, Pageable pageable) {
        validateUser(userId);
        return transactionRepository.findByUserIdAndType(userId, type, pageable)
                .map(transactionMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PointTransactionDTO> getUserTransactionsInDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        validateUser(userId);
        return transactionRepository.findUserTransactionsInDateRange(userId, startDate, endDate).stream()
                .map(transactionMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Integer calculateUserBalance(UUID userId) {
        validateUser(userId);
        return transactionRepository.calculateUserTotalPoints(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer calculateUserPointsByType(UUID userId, TransactionType type) {
        validateUser(userId);
        return transactionRepository.calculateUserPointsByType(userId, type);
    }

    private void validateUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }
    }
}