package org.xcyms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xcyms.common.ApiResult;
import org.xcyms.entity.User;
import org.xcyms.entity.dto.LoginDTO;
import org.xcyms.service.IUserService;

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
}
