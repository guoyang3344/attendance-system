package com.example.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.attendance.entity.LeaveApproval;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LeaveApprovalMapper extends BaseMapper<LeaveApproval> {

    List<LeaveApproval> selectByLeaveId(@Param("leaveId") Long leaveId);

    LeaveApproval selectLastApprovalByLeaveId(@Param("leaveId") Long leaveId);
}
