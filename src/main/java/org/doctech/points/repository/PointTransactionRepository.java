package org.doctech.points.repository;

import org.doctech.points.model.PointTransaction;
import org.doctech.points.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PointTransactionRepository extends JpaRepository<PointTransaction, UUID> {

    Page<PointTransaction> findByUserIdAndType(UUID userId, TransactionType type, Pageable pageable);

    @Query("SELECT pt FROM PointTransaction pt WHERE pt.user.id = :userId " +
            "AND pt.transactionDate BETWEEN :startDate AND :endDate")
    List<PointTransaction> findUserTransactionsInDateRange(UUID userId,
                                                           LocalDateTime startDate,
                                                           LocalDateTime endDate);

    @Query("SELECT SUM(pt.points) FROM PointTransaction pt WHERE pt.user.id = :userId AND pt.successful = true")
    Integer calculateUserTotalPoints(UUID userId);

    @Query("SELECT SUM(pt.points) FROM PointTransaction pt WHERE pt.user.id = :userId " +
            "AND pt.type = :type AND pt.successful = true")
    Integer calculateUserPointsByType(UUID userId, TransactionType type);

    @Query("SELECT pt FROM PointTransaction pt " +
            "WHERE pt.user.id = :userId " +
            "ORDER BY pt.transactionDate DESC")
    Page<PointTransaction> findByUserIdOrderByTransactionDateDesc(UUID userId, Pageable pageable);

    @Query("SELECT pt FROM PointTransaction pt " +
            "WHERE pt.user.id = :userId " +
            "ORDER BY pt.transactionDate DESC")
    List<PointTransaction> findByUserIdOrderByTransactionDateDesc(UUID userId);
}