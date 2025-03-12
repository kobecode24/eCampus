package org.doctech.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserStatisticsDTO {
    private long totalActiveUsers;
    private long newUsersToday;
    private long totalStudents;
    private long totalInstructors;
    private double avgEngagement;
} 