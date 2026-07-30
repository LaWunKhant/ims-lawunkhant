package com.cmps.ims.controller;

import com.cmps.ims.entity.Attendance;
import com.cmps.ims.security.CustomUserDetails;
import com.cmps.ims.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @GetMapping("/attendance")
    public String index(@AuthenticationPrincipal CustomUserDetails userDetails,
                         @PageableDefault(size = 10, page = 0, sort = "clockIn", direction = Sort.Direction.DESC) Pageable pageable,
                         Model model) {

        Integer userId = userDetails.getUser().getId();

        Page<Attendance> attendancePage = attendanceService.getHistoryForUser(userId, pageable);
        boolean clockedIn = attendanceService.isClockedIn(userId);

        model.addAttribute("attendancePage", attendancePage);
        model.addAttribute("clockedIn", clockedIn);

        return "attendance/index";
    }

    @PostMapping("/attendance/clock-in")
    public String clockIn(@AuthenticationPrincipal CustomUserDetails userDetails,
                           RedirectAttributes redirectAttributes) {
        try {
            attendanceService.clockIn(userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "出勤しました。");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/attendance";
    }

    @PostMapping("/attendance/clock-out")
    public String clockOut(@AuthenticationPrincipal CustomUserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        try {
            attendanceService.clockOut(userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "退勤しました。");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/attendance";
    }
}