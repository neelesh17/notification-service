package com.neelesh.noftification_service.service;

import com.neelesh.noftification_service.enums.Channel;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class MetricService {
    private final MeterRegistry meterRegistry;

    public void recordDelivery(Channel channel, String  status){
        Counter.builder("notification.total")
                .tag("channel", channel.name())
                .tag("status", status)
                .register(meterRegistry)
                .increment();
    }

    public void recordDeliveryTime(Channel channel, Duration duration){
        Timer.builder("notification.delivery.time")
                .tag("channel", channel.name())
                .register(meterRegistry)
                .record(duration);
    }
}
