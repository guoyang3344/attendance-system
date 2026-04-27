package com.example.attendance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.attendance.common.Result;
import com.example.attendance.dto.LeaveApprovalDTO;
import com.example.attendance.dto.LeaveSubmitDTO;
import com.example.attendance.entity.LeaveApplication;
import com.example.attendance.entity.LeaveType;
import com.example.attendance.entity.User;
import com.example.attendance.service.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leave")
public class LeaveController {

    @Autowired
    private LeaveService leaveService;

    @GetMapping("/types")
    public Result<List<LeaveType>> getLeaveTypes() {
        List<LeaveType> types = leaveService.getLeaveTypes();
        return Result.success(types);
    }

    @PostMapping("/submit")
    public Result<LeaveApplication> submitLeave(@Valid @RequestBody LeaveSubmitDTO dto, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "未登录或登录已过期");
        }
        
        try {
            LeaveApplication leave = leaveService.submitLeave(user, dto);
            return Result.success(leave);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/self")
    public Result<IPage<LeaveApplication>> getMyLeaves(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "未登录或登录已过期");
        }
        
        IPage<LeaveApplication> leaves = leaveService.getMyLeaves(user, page, size);
        return Result.success(leaves);
    }

    @GetMapping("/pending")
    public Result<IPage<LeaveApplication>> getPendingLeaves(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "未登录或登录已过期");
        }
        
        IPage<LeaveApplication> leaves = leaveService.getPendingLeaves(user, page, size);
        return Result.success(leaves);
    }

    @GetMapping("/all")
    public Result<IPage<LeaveApplication>> getAllLeaves(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "未登录或登录已过期");
        }
        
        boolean hasPermission = false;
        if (user.getPermissions() != null) {
            hasPermission = user.getPermissions().contains("leave:view:all");
        }
        
        if (!hasPermission) {
            return Result.error(403, "没有权限查看所有请假记录");
        }
        
        IPage<LeaveApplication> leaves = leaveService.getAllLeaves(page, size);
        return Result.success(leaves);
    }

    @GetMapping("/detail/{id}")
    public Result<LeaveApplication> getLeaveDetail(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "未登录或登录已过期");
        }
        
        LeaveApplication leave = leaveService.getLeaveDetail(id, user);
        if (leave == null) {
            return Result.error("请假记录不存在");
        }
        
        return Result.success(leave);
    }

    @PostMapping("/approve")
    public Result<LeaveApplication> approveLeave(@Valid @RequestBody LeaveApprovalDTO dto, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "未登录或登录已过期");
        }
        
        try {
            LeaveApplication leave = leaveService.approveLeave(user, dto);
            return Result.success(leave);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/my-approval-level")
    public Result<Map<String, Object>> getMyApprovalLevel(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "未登录或登录已过期");
        }
        
        Integer level = leaveService.getUserApprovalLevel(user);
        Map<String, Object> result = new HashMap<>();
        result.put("level", level);
        
        if (level != null) {
            String nextApproverName = leaveService.getNextApproverName(level);
            result.put("nextApproverName", nextApproverName);
        }
        
        return Result.success(result);
    }
}
