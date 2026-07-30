package com.cmps.ims.service;

import com.cmps.ims.entity.Attendance;
import com.cmps.ims.entity.User;
import com.cmps.ims.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttendanceService {

    private static final BigDecimal OVERTIME_THRESHOLD_HOURS = BigDecimal.valueOf(8);

    @Autowired
    private AttendanceRepository attendanceRepository;

    public Page<Attendance> getHistoryForUser(Integer userId, Pageable pageable) {
        return attendanceRepository.findByUser_Id(userId, pageable);
    }

    public boolean isClockedIn(Integer userId) {
        return attendanceRepository
                .findFirstByUser_IdAndClockOutIsNullOrderByClockInDesc(userId)
                .isPresent();
    }
    
    public boolean isOvertimeExceeded(Integer userId) {
        return attendanceRepository
                .findFirstByUser_IdAndClockOutIsNullOrderByClockInDesc(userId)
                .map(a -> Duration.between(a.getClockIn(), LocalDateTime.now()).toHours() >= 12)
                .orElse(false);
    }

    public void clockIn(User user) {
        if (isClockedIn(user.getId())) {
            throw new IllegalStateException("既に出勤済みです。");
        }
        Attendance attendance = new Attendance();
        attendance.setUser(user);
        attendance.setClockIn(LocalDateTime.now());
        attendance.setCreatedMemberId(user.getId());
        attendance.setUpdateMemberId(user.getId());
        attendanceRepository.save(attendance);
    }

    public void clockOut(User user) {
        Attendance attendance = attendanceRepository
                .findFirstByUser_IdAndClockOutIsNullOrderByClockInDesc(user.getId())
                .orElseThrow(() -> new IllegalStateException("出勤記録が見つかりません。"));

        LocalDateTime now = LocalDateTime.now();
        attendance.setClockOut(now);

        BigDecimal[] hours = calculateHours(attendance.getClockIn(), now);
        attendance.setWorkingHours(hours[0]);
        attendance.setOvertimeHours(hours[1]);
        attendance.setUpdateMemberId(user.getId());

        attendanceRepository.save(attendance);
    }

    private BigDecimal[] calculateHours(LocalDateTime clockIn, LocalDateTime clockOut) {
        long minutes = Duration.between(clockIn, clockOut).toMinutes();
        BigDecimal totalHours = BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        BigDecimal workingHours = totalHours.min(OVERTIME_THRESHOLD_HOURS);
        BigDecimal overtimeHours = totalHours.subtract(workingHours).max(BigDecimal.ZERO);

        return new BigDecimal[] { workingHours, overtimeHours };
    }
}