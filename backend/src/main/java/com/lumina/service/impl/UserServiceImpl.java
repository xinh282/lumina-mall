package com.lumina.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumina.common.BusinessException;
import com.lumina.dto.LoginDTO;
import com.lumina.dto.RegisterDTO;
import com.lumina.entity.User;
import com.lumina.mapper.UserMapper;
import com.lumina.security.JwtTokenProvider;
import com.lumina.service.UserService;
import com.lumina.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String USER_INFO_KEY = "user:info:";

    private UserVO getCached(String key) {
        Object obj = redisTemplate.opsForValue().get(key);
        if (obj == null) return null;
        if (obj instanceof UserVO vo) return vo;
        if (obj instanceof Map) {
            return objectMapper.convertValue(obj, UserVO.class);
        }
        redisTemplate.delete(key);
        return null;
    }

    @Override
    public void register(RegisterDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次密码不一致");
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        userMapper.insert(user);
    }

    @Override
    public Map<String, Object> login(LoginDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User user = userMapper.selectOne(wrapper);
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账户已被禁用");
        }
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        redisTemplate.opsForValue().set(USER_INFO_KEY + user.getId(), userVO, Duration.ofMinutes(30));

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", userVO);
        return result;
    }

    @Override
    public UserVO getProfile(Long userId) {
        String key = USER_INFO_KEY + userId;
        UserVO cached = getCached(key);
        if (cached != null) return cached;

        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        redisTemplate.opsForValue().set(key, vo, Duration.ofMinutes(30));
        return vo;
    }

    @Override
    public void updateProfile(Long userId, UserVO vo) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        if (vo.getNickname() != null) user.setNickname(vo.getNickname());
        if (vo.getEmail() != null) user.setEmail(vo.getEmail());
        if (vo.getPhone() != null) user.setPhone(vo.getPhone());
        if (vo.getAvatar() != null) user.setAvatar(vo.getAvatar());
        userMapper.updateById(user);
        redisTemplate.delete(USER_INFO_KEY + userId);
    }

    @Override
    public IPage<UserVO> adminList(int pageNum, int pageSize) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(User::getCreateTime);
        IPage<User> userPage = userMapper.selectPage(page, wrapper);
        return userPage.convert(user -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            return vo;
        });
    }

    @Override
    public void adminUpdateStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        user.setStatus(status);
        userMapper.updateById(user);
        redisTemplate.delete(USER_INFO_KEY + userId);
    }
}
