package com.example.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.attendance.entity.LeaveApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LeaveApplicationMapper extends BaseMapper<LeaveApplication> {

    List<LeaveApplication> selectByUserId(@Param("userId") Long userId);

    IPage<LeaveApplication> selectByUserId(IPage<LeaveApplication> page, @Param("userId") Long userId);

    IPage<LeaveApplication> selectPendingByApprover(IPage<LeaveApplication> page,
                                                     @Param("approverId") Long approverId,
                                                     @Param("approvalLevel") Integer approvalLevel);

    IPage<LeaveApplication> selectAllWithUser(IPage<LeaveApplication> page);

    LeaveApplication selectWithApprovalsById(@Param("id") Long id);
}
