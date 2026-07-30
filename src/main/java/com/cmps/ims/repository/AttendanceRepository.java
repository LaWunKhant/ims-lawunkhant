package com.cmps.ims.repository;

import com.cmps.ims.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {

    Page<Attendance> findByUser_Id(Integer userId, Pageable pageable);

    Optional<Attendance> findFirstByUser_IdAndClockOutIsNullOrderByClockInDesc(Integer userId);
}