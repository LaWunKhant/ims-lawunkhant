package com.cmps.ims.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendances")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "clock_in", nullable = false)
    private LocalDateTime clockIn;

    @Column(name = "clock_out")
    private LocalDateTime clockOut;

    @Column(name = "working_hours", precision = 5, scale = 2)
    private BigDecimal workingHours;

    @Column(name = "overtime_hours", precision = 5, scale = 2)
    private BigDecimal overtimeHours = BigDecimal.ZERO;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Column(name = "created_member_id")
    private Integer createdMemberId;

    @Column(name = "update_member_id")
    private Integer updateMemberId;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updateAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateAt = LocalDateTime.now();
    }

    // --- getters / setters ---

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getClockIn() { return clockIn; }
    public void setClockIn(LocalDateTime clockIn) { this.clockIn = clockIn; }

    public LocalDateTime getClockOut() { return clockOut; }
    public void setClockOut(LocalDateTime clockOut) { this.clockOut = clockOut; }

    public BigDecimal getWorkingHours() { return workingHours; }
    public void setWorkingHours(BigDecimal workingHours) { this.workingHours = workingHours; }

    public BigDecimal getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(BigDecimal overtimeHours) { this.overtimeHours = overtimeHours; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdateAt() { return updateAt; }

    public Integer getCreatedMemberId() { return createdMemberId; }
    public void setCreatedMemberId(Integer createdMemberId) { this.createdMemberId = createdMemberId; }

    public Integer getUpdateMemberId() { return updateMemberId; }
    public void setUpdateMemberId(Integer updateMemberId) { this.updateMemberId = updateMemberId; }
}