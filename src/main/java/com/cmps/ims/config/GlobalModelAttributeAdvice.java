package com.cmps.ims.config;

import com.cmps.ims.security.CustomUserDetails;
import com.cmps.ims.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributeAdvice {

    private final AttendanceService attendanceService;

    @ModelAttribute("loginUserName")
    public String loginUserName() {
        CustomUserDetails userDetails = getCurrentUserDetails();
        return userDetails != null ? userDetails.getUser().getName() : "";
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin() {
        CustomUserDetails userDetails = getCurrentUserDetails();
        if (userDetails == null) return false;
        Integer role = userDetails.getUser().getRole();
        return role != null && role == 1;
    }

    @ModelAttribute("isClockedIn")
    public boolean isClockedIn() {
        CustomUserDetails userDetails = getCurrentUserDetails();
        if (userDetails == null) return false;
        return attendanceService.isClockedIn(userDetails.getUser().getId());
    }
    
    @ModelAttribute("isOvertimeExceeded")
    public boolean isOvertimeExceeded() {
        CustomUserDetails userDetails = getCurrentUserDetails();
        if (userDetails == null) return false;
        return attendanceService.isOvertimeExceeded(userDetails.getUser().getId());
    }

    @ModelAttribute("canAccessMenu")
    public boolean canAccessMenu() {
        return isClockedIn() && !isOvertimeExceeded();
    }

    private CustomUserDetails getCurrentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails;
        }
        return null;
    }
}