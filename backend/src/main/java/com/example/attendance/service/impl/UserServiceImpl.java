package com.example.attendance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.attendance.entity.User;
import com.example.attendance.mapper.UserMapper;
import com.example.attendance.service.UserService;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

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
