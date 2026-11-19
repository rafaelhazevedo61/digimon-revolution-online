package com.dro.modules.activitycalendar.api;

import com.dro.modules.activitycalendar.api.dto.response.ActivityCalendarResponse;
import com.dro.modules.activitycalendar.application.ActivityCalendarService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@RequestMapping("/activity-calendar")
public class ActivityCalendarController {
    private final ActivityCalendarService service;
    @GetMapping("/current")
    public ResponseEntity<ActivityCalendarResponse> current(@RequestHeader("Authorization") String authorization) { return ResponseEntity.ok(service.current(authorization)); }
    @PostMapping("/days/{date}/claim")
    public ResponseEntity<ActivityCalendarResponse> claimDay(@RequestHeader("Authorization") String authorization, @PathVariable LocalDate date) { return ResponseEntity.ok(service.claimDay(authorization, date)); }
    @PostMapping("/months/{yearMonth}/claim-completion")
    public ResponseEntity<ActivityCalendarResponse> claimMonthly(@RequestHeader("Authorization") String authorization, @PathVariable YearMonth yearMonth) { return ResponseEntity.ok(service.claimMonthly(authorization, yearMonth)); }
    public ActivityCalendarController(ActivityCalendarService service) { this.service = service; }
}
