package com.neelesh.noftification_service.service;

import com.neelesh.noftification_service.job.RetryNotificationJob;
import com.neelesh.noftification_service.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DndSchedulerService {
    private final Scheduler scheduler;

    private ZonedDateTime calculateRetryTime(LocalTime dndEnd, LocalTime dndStart, String timezone) {
        ZoneId zone = ZoneId.of(timezone);
        LocalDate today = LocalDate.now(zone);

        // if window crosses midnight, dndEnd is tomorrow
        if (dndStart.isAfter(dndEnd)) {
            return ZonedDateTime.of(today.plusDays(1), dndEnd, zone);
        }
        return ZonedDateTime.of(today, dndEnd, zone);
    }

    public void scheduleRetry(Notification notification, LocalTime dndEnd, LocalTime dndStart, String timezone) throws SchedulerException {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("notificationId", notification.getId());
        JobDetail jobDetail = JobBuilder.newJob(RetryNotificationJob.class)
                .withIdentity("retry-" + notification.getId())
                .usingJobData(jobDataMap)
                .build();
        ZonedDateTime retryTime = calculateRetryTime(dndEnd, dndStart, timezone);
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger-" + notification.getId())
                .startAt(Date.from(retryTime.toInstant()))
                .build();
        scheduler.scheduleJob(jobDetail,trigger);
    }
}
