package com.example.pharmaaggregatorserver.dto.order;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OrderStatusHistoryResponseDTO {
    private Long historyId;
    private String fromStatus;
    private String toStatus;
    private String changedByRole;
    private String changedById;
    private String comment;
    private LocalDateTime changedAt;
}
