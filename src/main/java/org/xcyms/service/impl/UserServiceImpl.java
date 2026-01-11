package org.xcyms.service.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xcyms.common.ApiResult;
import org.xcyms.common.Constant;
import org.xcyms.entity.User;
import org.xcyms.entity.dto.LoginDTO;
import org.xcyms.mapper.UserMapper;
import org.xcyms.service.IUserService;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-11
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public ApiResult<?> register(User user) {
        // 1. 检查用户名是否已存在
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, user.getUsername()));
        if (count > 0) {
            return ApiResult.error("用户名已存在");
        }
        user.setNickname("用户" + RandomUtil.randomInt(1000, 10000));

        // 2. 密码加密 (使用 md5 加盐作为示例，实际推荐 BCrypt)
        String secretPassword = SaSecureUtil.md5BySalt(user.getPassword(), Constant.SALT);
        user.setPassword(secretPassword);

        // 3. 保存用户
        baseMapper.insert(user);
        return ApiResult.success("注册成功");
    }

    @Override
    public ApiResult<?> login(LoginDTO loginDto) {
        // 1. 查询用户
        User user = baseMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, loginDto.getUsername()));

        if (user == null) {
            return ApiResult.error("用户不存在");
        }

        // 2. 校验密码
        String secretPassword = SaSecureUtil.md5BySalt(loginDto.getPassword(), Constant.SALT);
        if (!user.getPassword().equals(secretPassword)) {
            return ApiResult.error("密码错误");
        }

        // 3. 执行 Sa-Token 登录
        StpUtil.login(user.getId());

        // 4. 返回 Token 信息
        return ApiResult.success(StpUtil.getTokenInfo().getTokenValue());
    }
}
