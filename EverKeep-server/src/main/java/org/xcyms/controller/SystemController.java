package org.xcyms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.xcyms.common.ApiResult;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统控制器
 */
@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
public class SystemController {

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    /**
     * 获取所有接口列表
     */
    @GetMapping("/endpoints")
    public ApiResult<?> getEndpoints() {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
        List<Map<String, Object>> endpoints = new ArrayList<>();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            RequestMappingInfo info = entry.getKey();
            HandlerMethod method = entry.getValue();

            // 排除 Spring Boot 默认接口和本接口
            String className = method.getBeanType().getName();
            if (className.startsWith("org.springframework") || className.contains("SystemController")) {
                continue;
            }

            Map<String, Object> map = new HashMap<>();
            map.put("name", method.getMethod().getName());
            map.put("controller", method.getBeanType().getSimpleName());

            // 获取路径
            Set<String> patterns = info.getDirectPaths();
            if (patterns.isEmpty()) {
                patterns = info.getPatternValues();
            }
            map.put("path", patterns);

            // 获取请求方法
            map.put("methods", info.getMethodsCondition().getMethods().stream()
                    .map(Enum::name)
                    .collect(Collectors.toSet()));

            endpoints.add(map);
        }

        // 按控制器名称排序
        endpoints.sort(Comparator.comparing(m -> (String) m.get("controller")));

        return ApiResult.success(endpoints);
    }
}