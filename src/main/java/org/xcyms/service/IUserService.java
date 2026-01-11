package org.xcyms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xcyms.common.ApiResult;
import org.xcyms.entity.User;
import org.xcyms.entity.dto.LoginDTO;

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
}
