package com.volunteer.management.hourslog.dto;

import java.util.List;

public record HoursSummaryResponse(Double totalHours, List<HoursLogResponse> history) {
}
