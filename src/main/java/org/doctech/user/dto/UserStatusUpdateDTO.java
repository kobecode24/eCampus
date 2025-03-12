package org.doctech.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserStatusUpdateDTO {
    @NotNull(message = "Enabled status is required")
    private Boolean enabled;
} 