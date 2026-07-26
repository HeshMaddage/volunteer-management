package com.volunteer.management.hourslog;

import com.volunteer.management.hourslog.dto.HoursSummaryResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/volunteers/me/hours")
public class HoursLogController {

    private final HoursLogService hoursLogService;

    public HoursLogController(HoursLogService hoursLogService) {
        this.hoursLogService = hoursLogService;
    }

    @GetMapping
    public HoursSummaryResponse getMyHours(Authentication authentication) {
        return hoursLogService.getMyHours(authentication.getName());
    }
}