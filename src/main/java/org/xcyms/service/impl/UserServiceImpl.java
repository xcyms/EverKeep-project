package org.xcyms.service.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.xcyms.common.ApiResult;
import org.xcyms.common.Constant;
import org.xcyms.entity.User;
import org.xcyms.entity.UserRole;
import org.xcyms.entity.dto.LoginDTO;
import org.xcyms.entity.dto.UserDTO;
import org.xcyms.mapper.RoleMapper;
import org.xcyms.mapper.UserMapper;
import org.xcyms.mapper.UserRoleMapper;
import org.xcyms.service.IUserService;

import java.util.List;
import java.util.stream.Collectors;

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
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final ModelMapper mapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;

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

        //4. 保存用户角色
        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(Constant.Role.USER);
        userRoleMapper.insert(userRole);
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

    /**
     * 获取用户信息
     *
     * @param loginId sa-token 的登录ID
     * @return org.xcyms.common.ApiResult<?>
     * @author liu-xu
     * @date 2026/1/12 9:55
     */
    @Override
    public ApiResult<?> getUserInfo(Long loginId) {
        User user = baseMapper.selectById(loginId);
        UserDTO userDto = mapper.map(user, UserDTO.class);

        // 获取角色列表
        List<String> roles = roleMapper.getRoleKeysByUserId(loginId);
        userDto.setRoles(roles);

        return ApiResult.success(userDto);
    }

    @Override
    public ApiResult<?> getPage(Page<User> page, UserDTO userDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (userDTO != null) {
            wrapper.like(StringUtils.isNotBlank(userDTO.getUsername()), User::getUsername, userDTO.getUsername())
                    .like(StringUtils.isNotBlank(userDTO.getNickname()), User::getNickname, userDTO.getNickname());
        }
        Page<User> userPage = this.page(page, wrapper);

        List<UserDTO> dtos = userPage.getRecords().stream().map(user -> {
            UserDTO dto = mapper.map(user, UserDTO.class);
            dto.setRoles(roleMapper.getRoleKeysByUserId(user.getId()));
            return dto;
        }).collect(Collectors.toList());

        Page<UserDTO> resultPage = new Page<>(page.getCurrent(), page.getSize(), userPage.getTotal());
        resultPage.setRecords(dtos);

        return ApiResult.success(resultPage);
    }

    @Override
    public ApiResult<?> updateProfile(UserDTO userDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = baseMapper.selectById(userId);
        if (user == null) {
            return ApiResult.error("用户不存在");
        }
        user.setNickname(userDTO.getNickname());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setAvatar(userDTO.getAvatar());
        baseMapper.updateById(user);
        return ApiResult.success("资料更新成功");
    }

    @Override
    public ApiResult<?> updatePassword(String oldPassword, String newPassword) {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = baseMapper.selectById(userId);
        if (user == null) {
            return ApiResult.error("用户不存在");
        }
        String oldSecretPassword = SaSecureUtil.md5BySalt(oldPassword, Constant.SALT);
        if (!user.getPassword().equals(oldSecretPassword)) {
            return ApiResult.error("原密码错误");
        }
        String newSecretPassword = SaSecureUtil.md5BySalt(newPassword, Constant.SALT);
        user.setPassword(newSecretPassword);
        baseMapper.updateById(user);
        return ApiResult.success("密码修改成功");
    }
}