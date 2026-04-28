package com.example.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.attendance.entity.User;
import com.example.attendance.entity.UserRole;
import com.example.attendance.mapper.UserMapper;
import com.example.attendance.mapper.UserRoleMapper;
import com.example.attendance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public User login(String username, String password) {
        User user = getUserWithRolesByUsername(username);
        if (user == null) {
            return null;
        }
        
        String encryptedPassword = encryptPassword(password);
        if (!user.getPassword().equals(encryptedPassword)) {
            return null;
        }
        
        if (user.getStatus() != 1) {
            return null;
        }
        
        List<String> permissions = getPermissionsByUserId(user.getId());
        user.setPermissions(permissions);
        
        return user;
    }

    @Override
    public User getUserWithRolesByUsername(String username) {
        List<User> users = baseMapper.selectUserWithRolesByUsername(username);
        if (users == null || users.isEmpty()) {
            return null;
        }
        return users.get(0);
    }

    @Override
    public User getUserWithRolesById(Long id) {
        return baseMapper.selectUserWithRolesById(id);
    }

    @Override
    public List<String> getPermissionsByUserId(Long userId) {
        return baseMapper.selectPermissionsByUserId(userId);
    }

    @Override
    public List<User> getAllUsersWithDetails() {
        return baseMapper.selectAllUsersWithDetails();
    }

    @Override
    public User getUserByIdWithDetails(Long id) {
        return baseMapper.selectUserByIdWithDetails(id);
    }

    @Override
    public List<User> getUsersByParentId(Long parentId) {
        return baseMapper.selectUsersByParentId(parentId);
    }

    @Override
    public List<User> getUsersByDeptId(Long deptId) {
        return baseMapper.selectUsersByDeptId(deptId);
    }

    @Override
    public User getSupervisorById(Long id) {
        return baseMapper.selectSupervisorById(id);
    }

    @Override
    public List<User> getSubordinatesById(Long id) {
        return baseMapper.selectSubordinatesById(id);
    }

    @Override
    public List<User> getUserTree() {
        return baseMapper.selectUserTree();
    }

    @Override
    @Transactional
    public boolean createUser(User user, List<Long> roleIds) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(encryptPassword(user.getPassword()));
        } else {
            user.setPassword(encryptPassword("123456"));
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        boolean saved = save(user);
        if (saved && roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                UserRole userRole = new UserRole();
                userRole.setUserId(user.getId());
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }
        return saved;
    }

    @Override
    @Transactional
    public boolean updateUser(User user, List<Long> roleIds) {
        User existingUser = getById(user.getId());
        if (existingUser == null) {
            return false;
        }
        if (user.getPassword() != null && !user.getPassword().isEmpty() && 
            !user.getPassword().equals(existingUser.getPassword())) {
            user.setPassword(encryptPassword(user.getPassword()));
        } else {
            user.setPassword(existingUser.getPassword());
        }
        boolean updated = updateById(user);
        if (updated && roleIds != null) {
            QueryWrapper<UserRole> deleteWrapper = new QueryWrapper<>();
            deleteWrapper.eq("user_id", user.getId());
            userRoleMapper.delete(deleteWrapper);
            for (Long roleId : roleIds) {
                UserRole userRole = new UserRole();
                userRole.setUserId(user.getId());
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }
        return updated;
    }

    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        QueryWrapper<UserRole> urWrapper = new QueryWrapper<>();
        urWrapper.eq("user_id", id);
        userRoleMapper.delete(urWrapper);
        return removeById(id);
    }

    @Override
    @Transactional
    public boolean assignRoles(Long userId, List<Long> roleIds) {
        QueryWrapper<UserRole> deleteWrapper = new QueryWrapper<>();
        deleteWrapper.eq("user_id", userId);
        userRoleMapper.delete(deleteWrapper);
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }
        return true;
    }

    @Override
    public boolean usernameExists(String username, Long excludeId) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username).eq("deleted", 0);
        if (excludeId != null) {
            queryWrapper.ne("id", excludeId);
        }
        return count(queryWrapper) > 0;
    }

    private String encryptPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }
}
