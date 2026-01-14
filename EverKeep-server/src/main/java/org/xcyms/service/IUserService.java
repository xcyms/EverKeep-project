package org.xcyms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.xcyms.common.ApiResult;
import org.xcyms.entity.User;
import org.xcyms.entity.dto.LoginDTO;
import org.xcyms.entity.dto.UserDTO;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-11
 */
public interface IUserService extends IService<User> {

    ApiResult<?> register(User user);

    ApiResult<?> login(LoginDTO loginDto);

    ApiResult<?> getUserInfo(Long loginId);

    ApiResult<?> getPage(Page<User> page, UserDTO userDTO);

    ApiResult<?> updateProfile(UserDTO userDTO);

    ApiResult<?> updatePassword(String oldPassword, String newPassword);
}