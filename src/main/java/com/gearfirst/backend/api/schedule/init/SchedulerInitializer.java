package com.gearfirst.backend.api.schedule.init;

import com.gearfirst.backend.api.schedule.entity.ScheduledTask;
import com.gearfirst.backend.api.schedule.enums.TaskStatus;
import com.gearfirst.backend.api.schedule.repository.ScheduledTaskRepository;
import com.gearfirst.backend.api.schedule.service.ScheduledTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SchedulerInitializer implements ApplicationRunner {
    private final ScheduledTaskRepository scheduledTaskRepository;
    private final ScheduledTaskService scheduledTaskService;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<ScheduledTask> pendingTasks =
                scheduledTaskRepository.findByStatusInAndRunAtAfter(
                        List.of(TaskStatus.PENDING, TaskStatus.RETRYING, TaskStatus.FAILED),
                        LocalDateTime.now()
                );
        //위에서 불러온 예약 대상 리스트(pendingTasks) 를 하나씩 순회하며 각 예약을 TaskScheduler.schedule()로 다시 등록
        for (ScheduledTask task : pendingTasks) {
            scheduledTaskService.registerTask(task);
        }
    }
}
