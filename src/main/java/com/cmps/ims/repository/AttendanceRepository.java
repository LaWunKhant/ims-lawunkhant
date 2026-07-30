package com.cmps.ims.repository;

import com.cmps.ims.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {

    List<Attendance> findByUser_IdOrderByClockInDesc(Integer userId);

    Optional<Attendance> findFirstByUser_IdAndClockOutIsNullOrderByClockInDesc(Integer userId);
}