-- 创建数据库
CREATE DATABASE IF NOT EXISTS attendance_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE attendance_system;

-- 部门表
CREATE TABLE IF NOT EXISTS sys_department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dept_name VARCHAR(100) NOT NULL COMMENT '部门名称',
    dept_code VARCHAR(50) NOT NULL UNIQUE COMMENT '部门编码',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID，0表示顶级部门',
    sort INT DEFAULT 0 COMMENT '排序',
    leader_id BIGINT COMMENT '部门负责人ID',
    description VARCHAR(500) COMMENT '部门描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    dept_id BIGINT COMMENT '部门ID',
    parent_id BIGINT COMMENT '上级用户ID',
    position VARCHAR(100) COMMENT '职位',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_dept_id (dept_id),
    INDEX idx_parent_id (parent_id)
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

-- 初始化部门数据
INSERT INTO sys_department (dept_name, dept_code, parent_id, sort, description) VALUES
('总公司', 'HEAD_OFFICE', 0, 1, '总公司'),
('技术部', 'TECH_DEPT', 1, 1, '负责技术研发和系统维护'),
('市场部', 'MARKET_DEPT', 1, 2, '负责市场推广和销售'),
('人事部', 'HR_DEPT', 1, 3, '负责人事管理和培训'),
('财务部', 'FINANCE_DEPT', 1, 4, '负责财务管理和预算'),
('开发一组', 'DEV_TEAM1', 2, 1, '负责产品A的开发'),
('开发二组', 'DEV_TEAM2', 2, 2, '负责产品B的开发'),
('运维组', 'OPS_TEAM', 2, 3, '负责系统运维和监控');

-- 初始化角色数据
INSERT INTO sys_role (role_name, role_code, description) VALUES
('员工', 'EMPLOYEE', '普通员工，可以打卡上下班'),
('组长', 'TEAM_LEADER', '组长，可以管理本组员工和查看本组打卡记录'),
('经理', 'MANAGER', '经理，可以管理部门员工和查看部门打卡记录'),
('董事长', 'CHAIRMAN', '董事长，可以查看所有员工的打卡记录');

-- 初始化权限数据
INSERT INTO sys_permission (id, permission_name, permission_code, resource_type, parent_id, path) VALUES
(1, '登录', 'system:login', 3, 0, '/api/login'),
(2, '打卡管理', 'attendance', 1, 0, NULL),
(3, '打卡', 'attendance:punch', 3, 2, '/api/attendance/punch'),
(4, '查看个人打卡记录', 'attendance:view:self', 3, 2, '/api/attendance/self'),
(5, '查看所有打卡记录', 'attendance:view:all', 3, 2, '/api/attendance/all'),
(6, '请假管理', 'leave', 1, 0, NULL),
(7, '申请请假', 'leave:apply', 3, 6, '/api/leave/submit'),
(8, '查看个人请假记录', 'leave:view:self', 3, 6, '/api/leave/self'),
(9, '查看待审批请假', 'leave:view:pending', 3, 6, '/api/leave/pending'),
(10, '审批请假', 'leave:approve', 3, 6, '/api/leave/approve'),
(11, '驳回请假', 'leave:reject', 3, 6, '/api/leave/reject'),
(12, '系统管理', 'system', 1, 0, NULL),
(13, '用户管理', 'system:user', 3, 12, '/api/user'),
(14, '角色管理', 'system:role', 3, 12, '/api/role'),
(15, '部门管理', 'system:department', 3, 12, '/api/department'),
(16, '查看组织架构', 'system:organization:view', 3, 12, '/api/organization');

-- 角色权限关联
-- 员工权限：登录、打卡、查看个人打卡记录、申请请假、查看个人请假记录、查看组织架构
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(1, 1), (1, 3), (1, 4), (1, 7), (1, 8), (1, 16),
-- 组长权限：员工所有权限 + 查看待审批请假、审批请假、驳回请假
(2, 1), (2, 3), (2, 4), (2, 7), (2, 8), (2, 9), (2, 10), (2, 11), (2, 16),
-- 经理权限：组长所有权限 + 查看所有打卡记录、用户管理、角色管理、部门管理
(3, 1), (3, 3), (3, 4), (3, 5), (3, 7), (3, 8), (3, 9), (3, 10), (3, 11), (3, 13), (3, 14), (3, 15), (3, 16),
-- 董事长权限：所有权限
(4, 1), (4, 3), (4, 4), (4, 5), (4, 7), (4, 8), (4, 9), (4, 10), (4, 11), (4, 13), (4, 14), (4, 15), (4, 16);

-- 初始化测试用户（密码为123456的MD5值：e10adc3949ba59abbe56e057f20f883e）
INSERT INTO sys_user (username, password, real_name, email, phone, status, dept_id, parent_id, position) VALUES
('chairman', 'e10adc3949ba59abbe56e057f20f883e', '刘董事长', 'liudongshizhang@example.com', '13800138005', 1, 1, NULL, '董事长'),
('manager', 'e10adc3949ba59abbe56e057f20f883e', '赵经理', 'zhaojingli@example.com', '13800138004', 1, 2, 1, '技术部经理'),
('teamleader', 'e10adc3949ba59abbe56e057f20f883e', '王组长', 'wangzuzhang@example.com', '13800138003', 1, 6, 2, '开发一组组长'),
('employee1', 'e10adc3949ba59abbe56e057f20f883e', '张三', 'zhangsan@example.com', '13800138001', 1, 6, 3, '开发工程师'),
('employee2', 'e10adc3949ba59abbe56e057f20f883e', '李四', 'lisi@example.com', '13800138002', 1, 6, 3, '开发工程师');

-- 更新部门负责人
UPDATE sys_department SET leader_id = 1 WHERE id = 1;
UPDATE sys_department SET leader_id = 2 WHERE id = 2;
UPDATE sys_department SET leader_id = 3 WHERE id = 6;

-- 用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),  -- 刘董事长 - 员工
(1, 4),  -- 刘董事长 - 董事长
(2, 1),  -- 赵经理 - 员工
(2, 3),  -- 赵经理 - 经理
(3, 1),  -- 王组长 - 员工
(3, 2),  -- 王组长 - 组长
(4, 1),  -- 张三 - 员工
(5, 1);  -- 李四 - 员工
