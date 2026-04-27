package com.example.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.attendance.entity.AttendanceRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface AttendanceRecordMapper extends BaseMapper<AttendanceRecord> {

    AttendanceRecord selectTodayRecordByUserId(@Param("userId") Long userId, @Param("today") LocalDate today);

    List<AttendanceRecord> selectByUserId(@Param("userId") Long userId);

    IPage<AttendanceRecord> selectAllRecordsWithUser(Page<AttendanceRecord> page);
}
