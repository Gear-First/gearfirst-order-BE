package com.gearfirst.backend.api.schedule.init;

import com.gearfirst.backend.api.schedule.entity.ScheduledTask;
import com.gearfirst.backend.api.schedule.enums.TaskStatus;
import com.gearfirst.backend.api.schedule.repository.ScheduledTaskRepository;
import com.gearfirst.backend.api.schedule.service.ScheduledTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchedulerInitializer implements ApplicationRunner {
    private final ScheduledTaskRepository scheduledTaskRepository;
    private final ScheduledTaskService scheduledTaskService;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        LocalDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime();
        List<ScheduledTask> pendingTasks =
                scheduledTaskRepository.findByStatusInAndRunAtLessThanEqual(
                        List.of(TaskStatus.PENDING, TaskStatus.RETRYING, TaskStatus.FAILED),
                        ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime()
                );
        //위에서 불러온 예약 대상 리스트(pendingTasks) 를 하나씩 순회하며 각 예약을 TaskScheduler.schedule()로 다시 등록
        for (ScheduledTask task : pendingTasks) {
            // 이미 실행 시점이 지났다면 바로 실행
            if (task.getRunAt().isBefore(now)) {
                log.warn("[지연된 작업 복원] orderId={}, 원래 실행 시각={}, 현재={}",
                        task.getOrderId(), task.getRunAt(), now);
                scheduledTaskService.executeTask(task, task.getOrderId());
            } else {
                // 아직 실행 전이라면 다시 예약
                scheduledTaskService.registerTask(task);
            }
        }
    }
}
