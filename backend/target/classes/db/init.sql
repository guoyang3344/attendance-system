-- 创建数据库
CREATE DATABASE IF NOT EXISTS attendance_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE attendance_system;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(200) COMMENT '角色描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100) NOT NULL COMMENT '权限编码',
    resource_type TINYINT DEFAULT 1 COMMENT '资源类型：1-菜单，2-按钮，3-接口',
    parent_id BIGINT DEFAULT 0 COMMENT '父权限ID',
    path VARCHAR(200) COMMENT '路由路径/接口路径',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户-角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色-权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_permission (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 打卡记录表
CREATE TABLE IF NOT EXISTS attendance_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    punch_in_time DATETIME COMMENT '上班打卡时间',
    punch_out_time DATETIME COMMENT '下班打卡时间',
    punch_date DATE NOT NULL COMMENT '打卡日期',
    status TINYINT DEFAULT 0 COMMENT '状态：0-正常，1-迟到，2-早退，3-旷工',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_user_id (user_id),
    INDEX idx_punch_date (punch_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡记录表';

-- 初始化角色数据
INSERT INTO sys_role (role_name, role_code, description) VALUES
('员工', 'EMPLOYEE', '普通员工，可以打卡上下班'),
('组长', 'TEAM_LEADER', '组长，可以管理本组员工和查看本组打卡记录'),
('经理', 'MANAGER', '经理，可以管理部门员工和查看部门打卡记录'),
('董事长', 'CHAIRMAN', '董事长，可以查看所有员工的打卡记录');

-- 初始化权限数据
INSERT INTO sys_permission (permission_name, permission_code, resource_type, path) VALUES
('登录', 'system:login', 3, '/api/login'),
('打卡', 'attendance:punch', 3, '/api/attendance/punch'),
('查看个人打卡记录', 'attendance:view:self', 3, '/api/attendance/self'),
('查看所有打卡记录', 'attendance:view:all', 3, '/api/attendance/all'),
('查看用户信息', 'system:user:view', 3, '/api/user/info');

-- 角色权限关联
-- 员工权限：登录、打卡、查看个人打卡记录、查看用户信息
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 5),
-- 组长权限：员工所有权限 + 查看本组打卡记录
(2, 1), (2, 2), (2, 3), (2, 5),
-- 经理权限：组长所有权限 + 查看部门打卡记录
(3, 1), (3, 2), (3, 3), (3, 5),
-- 董事长权限：所有权限
(4, 1), (4, 2), (4, 3), (4, 4), (4, 5);

-- 初始化测试用户（密码为123456的MD5值：e10adc3949ba59abbe56e057f20f883e）
INSERT INTO sys_user (username, password, real_name, email, phone, status) VALUES
('employee1', 'e10adc3949ba59abbe56e057f20f883e', '张三', 'zhangsan@example.com', '13800138001', 1),
('employee2', 'e10adc3949ba59abbe56e057f20f883e', '李四', 'lisi@example.com', '13800138002', 1),
('teamleader', 'e10adc3949ba59abbe56e057f20f883e', '王组长', 'wangzuzhang@example.com', '13800138003', 1),
('manager', 'e10adc3949ba59abbe56e057f20f883e', '赵经理', 'zhaojingli@example.com', '13800138004', 1),
('chairman', 'e10adc3949ba59abbe56e057f20f883e', '刘董事长', 'liudongshizhang@example.com', '13800138005', 1);

-- 用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),  -- 张三 - 员工
(2, 1),  -- 李四 - 员工
(3, 1),  -- 王组长 - 员工
(3, 2),  -- 王组长 - 组长
(4, 1),  -- 赵经理 - 员工
(4, 3),  -- 赵经理 - 经理
(5, 1),  -- 刘董事长 - 员工
(5, 4);  -- 刘董事长 - 董事长
