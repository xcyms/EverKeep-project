package org.xcyms.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.xcyms.common.ApiResult;
import org.xcyms.common.annotation.ApiDoc;
import org.xcyms.entity.User;
import org.xcyms.entity.dto.LoginDTO;
import org.xcyms.entity.dto.UserDTO;
import org.xcyms.service.IUserService;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-11
 */
@ApiDoc("用户管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final IUserService userService;

    @ApiDoc(value = "用户注册", notes = "新用户注册账号", order = 1)
    @PostMapping("/register")
    public ApiResult<String> register(@RequestBody User user) {
        return userService.register(user);
    }

    @ApiDoc(value = "用户登录", notes = "使用用户名和密码登录", order = 2)
    @PostMapping("/login")
    public ApiResult<String> login(@RequestBody LoginDTO loginDto) {
        return userService.login(loginDto);
    }

    @ApiDoc(value = "退出登录", order = 3)
    @GetMapping("/logout")
    public ApiResult<String> logout() {
        StpUtil.logout();
        return ApiResult.success("退出成功");
    }

    @ApiDoc(value = "获取个人信息", notes = "获取当前登录用户的详细信息", order = 4)
    @GetMapping("/info")
    public ApiResult<UserDTO> getUserInfo() {
        return userService.getUserInfo(StpUtil.getLoginIdAsLong());
    }

    @ApiDoc(value = "分页查询用户", notes = "管理员查看用户列表", order = 5)
    @SaCheckRole("ADMIN")
    @PostMapping("/page")
    public ApiResult<Page<UserDTO>> getPage(Page<User> page, @RequestBody UserDTO userDTO) {
        // 增加 id 作为二级排序，防止分页重复
        page.addOrder(OrderItem.desc("id"));
        return userService.getPage(page, userDTO);
    }

    @ApiDoc("更新个人资料")
    @PostMapping("/update")
    public ApiResult<String> updateProfile(@RequestBody UserDTO userDTO) {
        return userService.updateProfile(userDTO);
    }

    @ApiDoc("修改密码")
    @PostMapping("/password")
    public ApiResult<String> updatePassword(@RequestBody UserDTO userDTO) {
        return userService.updatePassword(userDTO);
    }
}