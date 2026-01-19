package org.xcyms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xcyms.common.ApiResult;
import org.xcyms.service.ISystemService;

import java.util.List;
import java.util.Map;

/**
 * 系统控制器
 */
@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
public class SystemController {

    private final ISystemService systemService;

    /**
     * 获取所有接口列表
     */
    @GetMapping("/endpoints")
    public ApiResult<List<Map<String, Object>>> getEndpoints() {
        return systemService.getEndpoints();
    }

    /**
     * 刷新接口列表缓存
     */
    @GetMapping("/endpoints/refresh")
    public ApiResult<String> refreshEndpoints() {
        systemService.refreshEndpoints();
        return ApiResult.success("刷新成功");
    }

}