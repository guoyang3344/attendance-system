-- 请假类型字典表
CREATE TABLE IF NOT EXISTS leave_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type_name VARCHAR(50) NOT NULL UNIQUE COMMENT '请假类型名称',
    type_code VARCHAR(50) NOT NULL UNIQUE COMMENT '请假类型编码',
    description VARCHAR(200) COMMENT '描述',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假类型表';

-- 请假申请表
CREATE TABLE IF NOT EXISTS leave_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '申请人ID',
    leave_type_id BIGINT NOT NULL COMMENT '请假类型ID',
    leave_type_name VARCHAR(50) NOT NULL COMMENT '请假类型名称（冗余字段）',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    leave_days DECIMAL(10,1) NOT NULL COMMENT '请假天数',
    reason VARCHAR(500) NOT NULL COMMENT '请假原因',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待审批，1-审批中，2-已通过，3-已驳回',
    current_approval_level TINYINT DEFAULT 1 COMMENT '当前审批级别：1-组长，2-经理，3-董事长',
    next_approver_id BIGINT COMMENT '下一个审批人ID（可以为null表示需要系统自动查找）',
    rejected_by BIGINT COMMENT '驳回人ID',
    rejected_comment VARCHAR(500) COMMENT '驳回意见',
    rejected_time DATETIME COMMENT '驳回时间',
    completed_time DATETIME COMMENT '完成时间（通过或驳回时间）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_current_approval_level (current_approval_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假申请表';

-- 请假审批记录表
CREATE TABLE IF NOT EXISTS leave_approval (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    leave_id BIGINT NOT NULL COMMENT '请假申请ID',
    approver_id BIGINT NOT NULL COMMENT '审批人ID',
    approver_name VARCHAR(50) NOT NULL COMMENT '审批人姓名（冗余字段）',
    approval_level TINYINT NOT NULL COMMENT '审批级别：1-组长，2-经理，3-董事长',
    approval_status TINYINT NOT NULL COMMENT '审批状态：1-通过，2-驳回',
    approval_comment VARCHAR(500) COMMENT '审批意见',
    approval_time DATETIME NOT NULL COMMENT '审批时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_leave_id (leave_id),
    INDEX idx_approver_id (approver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假审批记录表';

-- 初始化请假类型数据
INSERT INTO leave_type (type_name, type_code, description, sort_order) VALUES
('事假', 'PERSONAL', '因个人事务请假', 1),
('病假', 'SICK', '因生病请假', 2),
('年假', 'ANNUAL', '年度带薪休假', 3),
('婚假', 'MARRIAGE', '结婚请假', 4),
('产假', 'MATERNITY', '女性生育请假', 5),
('陪产假', 'PATERNITY', '男性陪产请假', 6),
('丧假', 'FUNERAL', '直系亲属离世请假', 7);

-- 添加请假相关权限
INSERT INTO sys_permission (permission_name, permission_code, resource_type, path) VALUES
('提交请假申请', 'leave:submit', 3, '/api/leave/submit'),
('查看自己的请假记录', 'leave:view:self', 3, '/api/leave/self'),
('查看请假详情', 'leave:view:detail', 3, '/api/leave/detail'),
('审批请假申请', 'leave:approve', 3, '/api/leave/approve'),
('驳回请假申请', 'leave:reject', 3, '/api/leave/reject'),
('查看待审批请假', 'leave:view:pending', 3, '/api/leave/pending'),
('查看所有请假记录', 'leave:view:all', 3, '/api/leave/all');

-- 角色权限关联更新
-- 员工权限：登录、打卡、查看个人打卡记录、查看用户信息、提交请假、查看自己的请假记录、查看请假详情
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(1, 6), (1, 7), (1, 8),

-- 组长权限：员工所有权限 + 审批请假申请、驳回请假申请、查看待审批请假
(2, 6), (2, 7), (2, 8), (2, 9), (2, 10), (2, 11),

-- 经理权限：组长所有权限
(3, 6), (3, 7), (3, 8), (3, 9), (3, 10), (3, 11),

-- 董事长权限：所有权限
(4, 6), (4, 7), (4, 8), (4, 9), (4, 10), (4, 11), (4, 12);
