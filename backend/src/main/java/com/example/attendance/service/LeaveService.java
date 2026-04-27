package com.example.attendance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.attendance.dto.LeaveApprovalDTO;
import com.example.attendance.dto.LeaveSubmitDTO;
import com.example.attendance.entity.LeaveApplication;
import com.example.attendance.entity.LeaveType;
import com.example.attendance.entity.User;

import java.util.List;

public interface LeaveService extends IService<LeaveApplication> {

    List<LeaveType> getLeaveTypes();

    LeaveApplication submitLeave(User user, LeaveSubmitDTO dto);

    IPage<LeaveApplication> getMyLeaves(User user, int page, int size);

    IPage<LeaveApplication> getPendingLeaves(User approver, int page, int size);

    IPage<LeaveApplication> getAllLeaves(int page, int size);

    LeaveApplication getLeaveDetail(Long id, User user);

    LeaveApplication approveLeave(User approver, LeaveApprovalDTO dto);

    Integer getUserApprovalLevel(User user);

    User getNextApprover(Integer currentLevel);

    String getNextApproverName(Integer currentLevel);
}
