package com.example.attendance.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.attendance.entity.AttendanceRecord;
import com.example.attendance.mapper.AttendanceRecordMapper;
import com.example.attendance.service.AttendanceService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AttendanceServiceImpl extends ServiceImpl<AttendanceRecordMapper, AttendanceRecord> implements AttendanceService {

    private static final LocalTime WORK_START_TIME = LocalTime.of(9, 0);
    private static final LocalTime WORK_END_TIME = LocalTime.of(18, 0);

    @Override
    public AttendanceRecord punchIn(Long userId) {
        LocalDate today = LocalDate.now();
        AttendanceRecord record = baseMapper.selectTodayRecordByUserId(userId, today);
        
        if (record == null) {
            record = new AttendanceRecord();
            record.setUserId(userId);
            record.setPunchDate(today);
            record.setStatus(0);
        }
        
        if (record.getPunchInTime() != null) {
            throw new RuntimeException("今天已经打过上班卡了");
        }
        
        LocalDateTime punchInTime = LocalDateTime.now();
        record.setPunchInTime(punchInTime);
        
        if (punchInTime.toLocalTime().isAfter(WORK_START_TIME)) {
            record.setStatus(1);
        }
        
        this.saveOrUpdate(record);
        return record;
    }

    @Override
    public AttendanceRecord punchOut(Long userId) {
        LocalDate today = LocalDate.now();
        AttendanceRecord record = baseMapper.selectTodayRecordByUserId(userId, today);
        
        if (record == null || record.getPunchInTime() == null) {
            throw new RuntimeException("今天还没有打上班卡，无法打下班卡");
        }
        
        if (record.getPunchOutTime() != null) {
            throw new RuntimeException("今天已经打过下班卡了");
        }
        
        LocalDateTime punchOutTime = LocalDateTime.now();
        record.setPunchOutTime(punchOutTime);
        
        if (punchOutTime.toLocalTime().isBefore(WORK_END_TIME)) {
            if (record.getStatus() == 0) {
                record.setStatus(2);
            } else {
                record.setStatus(3);
            }
        }
        
        this.updateById(record);
        return record;
    }

    @Override
    public AttendanceRecord getTodayRecord(Long userId) {
        LocalDate today = LocalDate.now();
        AttendanceRecord record = baseMapper.selectTodayRecordByUserId(userId, today);
        if (record != null) {
            record.setStatusName(getStatusName(record.getStatus()));
        }
        return record;
    }

    @Override
    public List<AttendanceRecord> getRecordsByUserId(Long userId) {
        List<AttendanceRecord> records = baseMapper.selectByUserId(userId);
        for (AttendanceRecord record : records) {
            record.setStatusName(getStatusName(record.getStatus()));
        }
        return records;
    }

    @Override
    public IPage<AttendanceRecord> getAllRecords(int page, int size) {
        Page<AttendanceRecord> pageParam = new Page<>(page, size);
        IPage<AttendanceRecord> resultPage = baseMapper.selectAllRecordsWithUser(pageParam);
        
        for (AttendanceRecord record : resultPage.getRecords()) {
            record.setStatusName(getStatusName(record.getStatus()));
        }
        
        return resultPage;
    }

    private String getStatusName(Integer status) {
        Map<Integer, String> statusMap = new HashMap<>();
        statusMap.put(0, "正常");
        statusMap.put(1, "迟到");
        statusMap.put(2, "早退");
        statusMap.put(3, "旷工");
        return statusMap.getOrDefault(status, "未知");
    }
}
