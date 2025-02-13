package org.doctech.points.service;

import org.doctech.points.dto.PointTransactionDTO;
import org.doctech.points.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PointTransactionService {
    PointTransactionDTO createTransaction(PointTransactionDTO transactionDTO);
    PointTransactionDTO rollbackTransaction(UUID transactionId);
    PointTransactionDTO getTransactionById(UUID id);
    List<PointTransactionDTO> getUserTransactionHistory(UUID userId);
    Page<PointTransactionDTO> getUserTransactionsByType(UUID userId, TransactionType type, Pageable pageable);
    List<PointTransactionDTO> getUserTransactionsInDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate);
    Integer calculateUserBalance(UUID userId);
    Integer calculateUserPointsByType(UUID userId, TransactionType type);
    PointTransactionDTO createTransaction(UUID userId, Integer points,
                                          TransactionType type, String description);
    void spendPoints(UUID userId, Integer points,
                     TransactionType type, String description);
    void validatePoints(UUID userId, Integer requiredPoints);
    Integer getUserBalance(UUID userId);
    Page<PointTransactionDTO> getUserTransactions(UUID userId, Pageable pageable);

}
