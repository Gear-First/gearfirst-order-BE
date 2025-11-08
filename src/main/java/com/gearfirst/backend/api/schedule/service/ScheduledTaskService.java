package com.gearfirst.backend.api.schedule.service;

import com.gearfirst.backend.api.order.entity.PurchaseOrder;
import com.gearfirst.backend.api.order.repository.PurchaseOrderRepository;
import com.gearfirst.backend.api.schedule.entity.ScheduledTask;
import com.gearfirst.backend.api.schedule.enums.TaskStatus;
import com.gearfirst.backend.api.schedule.repository.ScheduledTaskRepository;
import com.gearfirst.backend.common.exception.NotFoundException;
import com.gearfirst.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskService {
    private final TaskScheduler taskScheduler;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ScheduledTaskRepository scheduledTaskRepository;

    /**
     * 3일 뒤 실행 예약 등록
     */
    public void scheduleNewTask(Long orderId) {
        if (scheduledTaskRepository.existsByOrderIdAndStatusIn(orderId,
                List.of(TaskStatus.PENDING, TaskStatus.RETRYING))) {
            log.warn("[중복 예약] orderId={} 이미 등록된 예약이 있습니다.", orderId);
            return;
        }
        //LocalDateTime runAt = LocalDateTime.now().plusDays(3);
        LocalDateTime runAt = LocalDateTime.now().plusMinutes(3);

        ScheduledTask task = ScheduledTask.builder()
                .orderId(orderId)
                .runAt(runAt)
                .status(TaskStatus.PENDING)
                .retryCount(0)
                .build();

        scheduledTaskRepository.save(task);
        registerTask(task);
    }

    /**
     * TaskScheduler에 등록 (앱 시작 시 또는 새로 추가 시)
     */
    public void registerTask(ScheduledTask task) {
        log.info("[스케줄 등록] orderId={}, 실행 예정 시간={}", task.getOrderId(), task.getRunAt());

        taskScheduler.schedule(() -> executeTask(task , task.getOrderId()),
                Date.from(task.getRunAt().atZone(ZoneId.systemDefault()).toInstant()));
    }

    /**
     * 실제 작업 실행 + 재시도 로직
     */
    private void executeTask(ScheduledTask task, Long orderId) {
        try {
            PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                    .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_ORDER_EXCEPTION.getMessage()));
            log.info("[실행 시작] orderId={}", task.getOrderId());
            task.setStatus(TaskStatus.RUNNING);
            scheduledTaskRepository.save(task);

            // 성공 처리
            task.setStatus(TaskStatus.SUCCESS);
            task.setErrorMessage(null);
            scheduledTaskRepository.save(task);
            order.complete();
            purchaseOrderRepository.save(order); //Spring의 트랜잭션 프록시 밖의 별도 스레드에서 실행되므로
            log.info("[실행 성공] orderId={}", task.getOrderId());

        } catch (Exception e) {
            log.error("[실행 실패] orderId={}, error={}", task.getOrderId(), e.getMessage());

            task.setStatus(TaskStatus.FAILED);
            task.setErrorMessage(e.getMessage());
            task.setRetryCount(task.getRetryCount() + 1);

            if (task.getRetryCount() <= 3) {
                // 1시간 뒤 재시도
                LocalDateTime retryAt = LocalDateTime.now().plusHours(1);
                task.setStatus(TaskStatus.RETRYING);
                task.setRunAt(retryAt);
                scheduledTaskRepository.save(task);

                // 다시 스케줄 등록
                registerTask(task);
                log.info("[재시도 등록] orderId={}, {}회차, 재시도 시각={}",
                        task.getOrderId(), task.getRetryCount(), retryAt);
            } else {
                // 재시도 3회 초과 시 완전 실패
                scheduledTaskRepository.save(task);
                log.error("[재시도 초과] orderId={} 완전 실패 처리됨", task.getOrderId());
            }
        }
    }
}
