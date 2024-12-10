package org.doctech.points.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.doctech.points.model.TransactionType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointTransactionDTO {
    private UUID id;

    @NotNull(message = "User ID is required")
    private UUID userId;

    private String username;
    private UUID relatedItemId;

    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    @NotNull(message = "Points amount is required")
    private Integer points;

    private String description;
    private LocalDateTime transactionDate;
    private boolean successful;
    private UUID rollbackTransactionId;
}
