package com.gearfirst.backend.api.schedule.entity;

import com.gearfirst.backend.api.schedule.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_task")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;
    private LocalDateTime runAt;

    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.PENDING;

    private int retryCount = 0;
    private String errorMessage;
}


