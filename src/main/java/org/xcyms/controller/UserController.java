package org.xcyms.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.xcyms.common.ApiResult;
import org.xcyms.entity.User;
import org.xcyms.entity.dto.LoginDTO;
import org.xcyms.entity.dto.UserDTO;
import org.xcyms.service.IUserService;

import java.util.Map;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-11
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final IUserService userService;

    @PostMapping("/register")
    public ApiResult<?> register(@RequestBody User user) {
        return userService.register(user);
    }

    @PostMapping("/login")
    public ApiResult<?> login(@RequestBody LoginDTO loginDto) {
        return userService.login(loginDto);
    }

    @PostMapping("/logout")
    public ApiResult<?> logout() {
        StpUtil.logout();
        return ApiResult.success();
    }

    @GetMapping("/info")
    public ApiResult<?> getUserInfo() {
        return userService.getUserInfo(StpUtil.getLoginIdAsLong());
    }

    @SaCheckRole("ADMIN")
    @PostMapping("/page")
    public ApiResult<?> getPage(Page<User> page, @RequestBody UserDTO userDTO) {
        return userService.getPage(page, userDTO);
    }

    @PostMapping("/update")
    public ApiResult<?> updateProfile(@RequestBody UserDTO userDTO) {
        return userService.updateProfile(userDTO);
    }

    @PostMapping("/password")
    public ApiResult<?> updatePassword(@RequestBody Map<String, String> params) {
        return userService.updatePassword(params.get("oldPassword"), params.get("newPassword"));
    }
}