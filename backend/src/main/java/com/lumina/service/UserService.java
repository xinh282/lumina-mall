package com.lumina.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lumina.dto.LoginDTO;
import com.lumina.dto.RegisterDTO;
import com.lumina.vo.UserVO;
import java.util.Map;

public interface UserService {
    void register(RegisterDTO dto);
    Map<String, Object> login(LoginDTO dto);
    UserVO getProfile(Long userId);
    void updateProfile(Long userId, UserVO vo);
    IPage<UserVO> adminList(int page, int size);
    void adminUpdateStatus(Long userId, Integer status);
}
