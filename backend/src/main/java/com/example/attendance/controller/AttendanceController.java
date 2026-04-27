package com.example.attendance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.attendance.common.Result;
import com.example.attendance.entity.AttendanceRecord;
import com.example.attendance.entity.User;
import com.example.attendance.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/punchIn")
    public Result<AttendanceRecord> punchIn(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "未登录或登录已过期");
        }
        
        try {
            AttendanceRecord record = attendanceService.punchIn(user.getId());
            return Result.success(record);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/punchOut")
    public Result<AttendanceRecord> punchOut(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "未登录或登录已过期");
        }
        
        try {
            AttendanceRecord record = attendanceService.punchOut(user.getId());
            return Result.success(record);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/today")
    public Result<AttendanceRecord> getTodayRecord(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "未登录或登录已过期");
        }
        
        AttendanceRecord record = attendanceService.getTodayRecord(user.getId());
        return Result.success(record);
    }

    @GetMapping("/self")
    public Result<List<AttendanceRecord>> getSelfRecords(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "未登录或登录已过期");
        }
        
        List<AttendanceRecord> records = attendanceService.getRecordsByUserId(user.getId());
        return Result.success(records);
    }

    @GetMapping("/all")
    public Result<IPage<AttendanceRecord>> getAllRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "未登录或登录已过期");
        }
        
        boolean hasPermission = false;
        if (user.getPermissions() != null) {
            hasPermission = user.getPermissions().contains("attendance:view:all");
        }
        
        if (!hasPermission) {
            return Result.error(403, "没有权限查看所有打卡记录");
        }
        
        IPage<AttendanceRecord> records = attendanceService.getAllRecords(page, size);
        return Result.success(records);
    }
}
