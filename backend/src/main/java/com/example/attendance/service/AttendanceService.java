package com.example.attendance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.attendance.entity.AttendanceRecord;

import java.util.List;

public interface AttendanceService extends IService<AttendanceRecord> {

    AttendanceRecord punchIn(Long userId);

    AttendanceRecord punchOut(Long userId);

    AttendanceRecord getTodayRecord(Long userId);

    List<AttendanceRecord> getRecordsByUserId(Long userId);

    IPage<AttendanceRecord> getAllRecords(int page, int size);
}
