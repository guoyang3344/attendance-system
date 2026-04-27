package com.example.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.attendance.dto.LeaveApprovalDTO;
import com.example.attendance.dto.LeaveSubmitDTO;
import com.example.attendance.entity.*;
import com.example.attendance.enums.ApprovalLevelEnum;
import com.example.attendance.enums.ApprovalStatusEnum;
import com.example.attendance.enums.LeaveStatusEnum;
import com.example.attendance.mapper.*;
import com.example.attendance.service.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LeaveServiceImpl extends ServiceImpl<LeaveApplicationMapper, LeaveApplication> implements LeaveService {

    @Autowired
    private LeaveTypeMapper leaveTypeMapper;

    @Autowired
    private LeaveApprovalMapper leaveApprovalMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public List<LeaveType> getLeaveTypes() {
        LambdaQueryWrapper<LeaveType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LeaveType::getStatus, 1)
                .orderByAsc(LeaveType::getSortOrder);
        return leaveTypeMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LeaveApplication submitLeave(User user, LeaveSubmitDTO dto) {
        LeaveApplication leave = new LeaveApplication();
        leave.setUserId(user.getId());
        leave.setLeaveTypeId(dto.getLeaveTypeId());
        leave.setLeaveTypeName(dto.getLeaveTypeName());
        leave.setStartTime(dto.getStartTime());
        leave.setEndTime(dto.getEndTime());
        leave.setLeaveDays(dto.getLeaveDays());
        leave.setReason(dto.getReason());
        
        leave.setStatus(LeaveStatusEnum.PENDING.getCode());
        leave.setCurrentApprovalLevel(ApprovalLevelEnum.TEAM_LEADER.getCode());
        
        User nextApprover = getNextApproverByLevel(ApprovalLevelEnum.TEAM_LEADER.getCode());
        if (nextApprover != null) {
            leave.setNextApproverId(nextApprover.getId());
        }
        
        this.save(leave);
        
        leave.setStatusName(LeaveStatusEnum.getNameByCode(leave.getStatus()));
        leave.setCurrentApprovalLevelName(ApprovalLevelEnum.getNameByCode(leave.getCurrentApprovalLevel()));
        if (nextApprover != null) {
            leave.setNextApproverName(nextApprover.getRealName());
        }
        
        return leave;
    }

    @Override
    public IPage<LeaveApplication> getMyLeaves(User user, int page, int size) {
        Page<LeaveApplication> pageParam = new Page<>(page, size);
        IPage<LeaveApplication> resultPage = baseMapper.selectByUserId(pageParam, user.getId());
        
        for (LeaveApplication leave : resultPage.getRecords()) {
            enrichLeaveInfo(leave);
        }
        
        return resultPage;
    }

    @Override
    public IPage<LeaveApplication> getPendingLeaves(User approver, int page, int size) {
        Integer approvalLevel = getUserApprovalLevel(approver);
        if (approvalLevel == null) {
            return new Page<>();
        }
        
        Page<LeaveApplication> pageParam = new Page<>(page, size);
        IPage<LeaveApplication> resultPage = baseMapper.selectPendingByApprover(pageParam, approver.getId(), approvalLevel);
        
        for (LeaveApplication leave : resultPage.getRecords()) {
            enrichLeaveInfo(leave);
        }
        
        return resultPage;
    }

    @Override
    public IPage<LeaveApplication> getAllLeaves(int page, int size) {
        Page<LeaveApplication> pageParam = new Page<>(page, size);
        IPage<LeaveApplication> resultPage = baseMapper.selectAllWithUser(pageParam);
        
        for (LeaveApplication leave : resultPage.getRecords()) {
            enrichLeaveInfo(leave);
        }
        
        return resultPage;
    }

    @Override
    public LeaveApplication getLeaveDetail(Long id, User user) {
        LeaveApplication leave = baseMapper.selectWithApprovalsById(id);
        if (leave == null) {
            return null;
        }
        
        enrichLeaveInfo(leave);
        
        if (leave.getApprovals() != null) {
            for (LeaveApproval approval : leave.getApprovals()) {
                approval.setApprovalLevelName(ApprovalLevelEnum.getNameByCode(approval.getApprovalLevel()));
                approval.setApprovalStatusName(ApprovalStatusEnum.getNameByCode(approval.getApprovalStatus()));
            }
        }
        
        return leave;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LeaveApplication approveLeave(User approver, LeaveApprovalDTO dto) {
        LeaveApplication leave = this.getById(dto.getLeaveId());
        if (leave == null) {
            throw new RuntimeException("请假申请不存在");
        }
        
        if (leave.getStatus() != LeaveStatusEnum.PENDING.getCode() 
                && leave.getStatus() != LeaveStatusEnum.IN_PROGRESS.getCode()) {
            throw new RuntimeException("该请假申请已完成，无法审批");
        }
        
        Integer approverLevel = getUserApprovalLevel(approver);
        if (approverLevel == null) {
            throw new RuntimeException("您没有审批权限");
        }
        
        if (!approverLevel.equals(leave.getCurrentApprovalLevel())) {
            throw new RuntimeException("当前不在您的审批环节");
        }
        
        LeaveApproval approval = new LeaveApproval();
        approval.setLeaveId(leave.getId());
        approval.setApproverId(approver.getId());
        approval.setApproverName(approver.getRealName());
        approval.setApprovalLevel(approverLevel);
        approval.setApprovalStatus(dto.getApprovalStatus());
        approval.setApprovalComment(dto.getApprovalComment());
        approval.setApprovalTime(LocalDateTime.now());
        leaveApprovalMapper.insert(approval);
        
        if (ApprovalStatusEnum.REJECTED.getCode().equals(dto.getApprovalStatus())) {
            leave.setStatus(LeaveStatusEnum.REJECTED.getCode());
            leave.setRejectedBy(approver.getId());
            leave.setRejectedByName(approver.getRealName());
            leave.setRejectedComment(dto.getApprovalComment());
            leave.setRejectedTime(LocalDateTime.now());
            leave.setCompletedTime(LocalDateTime.now());
        } else {
            ApprovalLevelEnum nextLevel = ApprovalLevelEnum.getNextLevel(approverLevel);
            if (nextLevel == null) {
                leave.setStatus(LeaveStatusEnum.APPROVED.getCode());
                leave.setCompletedTime(LocalDateTime.now());
                leave.setCurrentApprovalLevel(null);
                leave.setNextApproverId(null);
            } else {
                leave.setStatus(LeaveStatusEnum.IN_PROGRESS.getCode());
                leave.setCurrentApprovalLevel(nextLevel.getCode());
                
                User nextApprover = getNextApproverByLevel(nextLevel.getCode());
                if (nextApprover != null) {
                    leave.setNextApproverId(nextApprover.getId());
                }
            }
        }
        
        this.updateById(leave);
        
        enrichLeaveInfo(leave);
        
        return leave;
    }

    @Override
    public Integer getUserApprovalLevel(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            User userWithRoles = userMapper.selectUserWithRolesById(user.getId());
            if (userWithRoles == null || userWithRoles.getRoles() == null) {
                return null;
            }
            user.setRoles(userWithRoles.getRoles());
        }
        
        Integer highestLevel = null;
        for (Role role : user.getRoles()) {
            String roleCode = role.getRoleCode();
            if ("CHAIRMAN".equals(roleCode)) {
                return ApprovalLevelEnum.CHAIRMAN.getCode();
            } else if ("MANAGER".equals(roleCode)) {
                if (highestLevel == null || highestLevel < ApprovalLevelEnum.MANAGER.getCode()) {
                    highestLevel = ApprovalLevelEnum.MANAGER.getCode();
                }
            } else if ("TEAM_LEADER".equals(roleCode)) {
                if (highestLevel == null) {
                    highestLevel = ApprovalLevelEnum.TEAM_LEADER.getCode();
                }
            }
        }
        
        return highestLevel;
    }

    @Override
    public User getNextApprover(Integer currentLevel) {
        ApprovalLevelEnum nextLevel = ApprovalLevelEnum.getNextLevel(currentLevel);
        if (nextLevel == null) {
            return null;
        }
        return getNextApproverByLevel(nextLevel.getCode());
    }

    @Override
    public String getNextApproverName(Integer currentLevel) {
        User approver = getNextApprover(currentLevel);
        return approver != null ? approver.getRealName() : null;
    }

    private User getNextApproverByLevel(Integer level) {
        String roleCode = null;
        if (ApprovalLevelEnum.TEAM_LEADER.getCode().equals(level)) {
            roleCode = "TEAM_LEADER";
        } else if (ApprovalLevelEnum.MANAGER.getCode().equals(level)) {
            roleCode = "MANAGER";
        } else if (ApprovalLevelEnum.CHAIRMAN.getCode().equals(level)) {
            roleCode = "CHAIRMAN";
        }
        
        if (roleCode == null) {
            return null;
        }
        
        LambdaQueryWrapper<Role> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.eq(Role::getRoleCode, roleCode);
        Role role = roleMapper.selectOne(roleWrapper);
        
        if (role == null) {
            return null;
        }
        
        LambdaQueryWrapper<UserRole> userRoleWrapper = new LambdaQueryWrapper<>();
        userRoleWrapper.eq(UserRole::getRoleId, role.getId());
        List<UserRole> userRoles = userRoleMapper.selectList(userRoleWrapper);
        
        if (userRoles == null || userRoles.isEmpty()) {
            return null;
        }
        
        return userMapper.selectById(userRoles.get(0).getUserId());
    }

    private void enrichLeaveInfo(LeaveApplication leave) {
        leave.setStatusName(LeaveStatusEnum.getNameByCode(leave.getStatus()));
        leave.setCurrentApprovalLevelName(ApprovalLevelEnum.getNameByCode(leave.getCurrentApprovalLevel()));
        
        if (leave.getNextApproverId() != null) {
            User nextApprover = userMapper.selectById(leave.getNextApproverId());
            if (nextApprover != null) {
                leave.setNextApproverName(nextApprover.getRealName());
            }
        }
        
        if (leave.getRejectedBy() != null) {
            User rejector = userMapper.selectById(leave.getRejectedBy());
            if (rejector != null) {
                leave.setRejectedByName(rejector.getRealName());
            }
        }
    }
}
