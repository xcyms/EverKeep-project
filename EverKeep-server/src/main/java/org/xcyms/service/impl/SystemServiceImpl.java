package org.xcyms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.xcyms.common.ApiResult;
import org.xcyms.common.annotation.ApiDoc;
import org.xcyms.common.annotation.ApiDocProperty;
import org.xcyms.service.ISystemService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统服务实现类
 */
@Service
@RequiredArgsConstructor
public class SystemServiceImpl implements ISystemService {

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Override
    @Cacheable(value = "system", key = "'endpoints'")
    public ApiResult<List<Map<String, Object>>> getEndpoints() {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
        List<Map<String, Object>> endpoints = new ArrayList<>();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            RequestMappingInfo info = entry.getKey();
            HandlerMethod handlerMethod = entry.getValue();

            Class<?> beanType = handlerMethod.getBeanType();
            Method method = handlerMethod.getMethod();

            // 排除 Spring Boot 默认接口和系统内部接口
            String className = beanType.getName();
            if (className.startsWith("org.springframework") ||
                    className.startsWith("com.github.xiaoymin") ||
                    className.contains("SystemController") ||
                    className.contains("BasicErrorController")) {
                continue;
            }

            ApiDoc classDoc = AnnotationUtils.findAnnotation(beanType, ApiDoc.class);
            ApiDoc methodDoc = AnnotationUtils.findAnnotation(method, ApiDoc.class);

            Map<String, Object> map = new HashMap<>();

            // 1. 基本信息
            String name = (methodDoc != null && !methodDoc.value().isEmpty()) ? methodDoc.value() : method.getName();
            map.put("name", name);
            map.put("methodName", method.getName());

            String group = (classDoc != null && !classDoc.value().isEmpty()) ? classDoc.value() : beanType.getSimpleName();
            map.put("controller", group);
            map.put("description", methodDoc != null ? methodDoc.notes() : "");
            map.put("order", methodDoc != null ? methodDoc.order() : 99);

            // 1.1 安全/权限信息 (Sa-Token)
            Map<String, Object> security = new HashMap<>();
            cn.dev33.satoken.annotation.SaCheckRole roleAnn = AnnotationUtils.findAnnotation(method, cn.dev33.satoken.annotation.SaCheckRole.class);
            if (roleAnn != null) security.put("roles", roleAnn.value());

            cn.dev33.satoken.annotation.SaCheckPermission permAnn = AnnotationUtils.findAnnotation(method, cn.dev33.satoken.annotation.SaCheckPermission.class);
            if (permAnn != null) security.put("permissions", permAnn.value());

            if (AnnotationUtils.findAnnotation(method, cn.dev33.satoken.annotation.SaCheckLogin.class) != null ||
                    AnnotationUtils.findAnnotation(beanType, cn.dev33.satoken.annotation.SaCheckLogin.class) != null) {
                security.put("requiresLogin", true);
            }
            map.put("security", security);

            // 2. 路径与方法
            Set<String> patterns = info.getDirectPaths();
            if (patterns.isEmpty()) patterns = info.getPatternValues();
            map.put("path", patterns);
            map.put("methods", info.getMethodsCondition().getMethods().stream().map(Enum::name).collect(Collectors.toSet()));

            // 3. 入参解析
            map.put("params", parseParameters(handlerMethod));

            // 4. 出参解析
            Type returnType = method.getGenericReturnType();
            map.put("response", parseType(returnType, new HashSet<>()));
            map.put("returnTypeName", getSimpleTypeName(returnType));

            endpoints.add(map);
        }

        // 排序：先按分组排序，再按 order 排序
        endpoints.sort(Comparator.comparing((Map<String, Object> m) -> (String) m.get("controller"))
                .thenComparingInt(m -> (int) m.get("order")));

        return ApiResult.success(endpoints);
    }

    @Override
    @CacheEvict(value = "system", key = "'endpoints'")
    public void refreshEndpoints() {
        // 此方法通过 @CacheEvict 清除缓存，下次调用 getEndpoints 时会自动重新加载
    }

    private String getSimpleTypeName(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            String rawName = ((Class<?>) pType.getRawType()).getSimpleName();
            String typeArgs = Arrays.stream(pType.getActualTypeArguments())
                    .map(this::getSimpleTypeName)
                    .collect(Collectors.joining(", "));
            return rawName + "<" + typeArgs + ">";
        } else if (type instanceof Class) {
            return ((Class<?>) type).getSimpleName();
        }
        return type.getTypeName();
    }

    private List<Map<String, Object>> parseParameters(HandlerMethod handlerMethod) {
        List<Map<String, Object>> params = new ArrayList<>();
        MethodParameter[] methodParameters = handlerMethod.getMethodParameters();

        for (MethodParameter parameter : methodParameters) {
            Class<?> paramType = parameter.getParameterType();
            if (paramType.getName().startsWith("jakarta.servlet") ||
                    paramType.getName().startsWith("org.springframework.ui")) {
                continue;
            }

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("name", parameter.getParameterName());
            paramMap.put("type", paramType.getSimpleName());

            boolean required = false;
            String defaultValue = null;

            if (parameter.hasParameterAnnotation(org.springframework.web.bind.annotation.RequestBody.class)) {
                paramMap.put("source", "Body");
                required = true;
            } else if (parameter.hasParameterAnnotation(org.springframework.web.bind.annotation.PathVariable.class)) {
                paramMap.put("source", "Path");
                required = true;
            } else if (parameter.hasParameterAnnotation(org.springframework.web.bind.annotation.RequestParam.class)) {
                org.springframework.web.bind.annotation.RequestParam ann = parameter.getParameterAnnotation(org.springframework.web.bind.annotation.RequestParam.class);
                paramMap.put("source", "Query");
                required = ann.required();
                if (!"\n\t\t\n\t\t\n\ue000\t\t\t\t\n".equals(ann.defaultValue())) defaultValue = ann.defaultValue();
            } else {
                paramMap.put("source", "Form/Query");
            }

            paramMap.put("required", required);
            if (defaultValue != null) paramMap.put("defaultValue", defaultValue);

            List<Map<String, Object>> schema = parseType(parameter.getGenericParameterType(), new HashSet<>());
            if (!schema.isEmpty()) {
                paramMap.put("schema", schema);
            }

            params.add(paramMap);
        }
        return params;
    }

    private List<Map<String, Object>> parseType(Type type, Set<Class<?>> visited) {
        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            Type rawType = pType.getRawType();
            if (rawType instanceof Class) {
                Class<?> clazz = (Class<?>) rawType;
                if (Collection.class.isAssignableFrom(clazz) || clazz.isArray()) {
                    return parseType(pType.getActualTypeArguments()[0], visited);
                }
                if (clazz.getSimpleName().equals("ApiResult") || clazz.getSimpleName().equals("IPage") || clazz.getSimpleName().equals("Page")) {
                    return parseType(pType.getActualTypeArguments()[0], visited);
                }
            }
        }

        if (!(type instanceof Class)) return Collections.emptyList();

        Class<?> clazz = (Class<?>) type;
        if (clazz.isPrimitive() || clazz.getName().startsWith("java.") || visited.contains(clazz)) {
            return Collections.emptyList();
        }

        visited.add(clazz);
        List<Map<String, Object>> fields = new ArrayList<>();

        Class<?> currentClass = clazz;
        while (currentClass != null && !currentClass.getName().startsWith("java.")) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
                if ("serialVersionUID".equals(field.getName())) continue;

                Map<String, Object> fieldMap = new HashMap<>();
                ApiDocProperty property = field.getAnnotation(ApiDocProperty.class);

                fieldMap.put("field", field.getName());
                fieldMap.put("type", field.getType().getSimpleName());

                String desc = property != null ? property.value() : "";
                if (field.getType().isEnum()) {
                    String enumValues = Arrays.toString(field.getType().getEnumConstants());
                    desc += (desc.isEmpty() ? "" : " ") + "可选值: " + enumValues;
                }
                fieldMap.put("description", desc);

                fieldMap.put("required", property != null && property.required());
                fieldMap.put("example", property != null ? property.example() : "");

                if (isComplexType(field.getType())) {
                    fieldMap.put("children", parseType(field.getGenericType(), new HashSet<>(visited)));
                }

                fields.add(fieldMap);
            }
            currentClass = currentClass.getSuperclass();
        }
        return fields;
    }

    private boolean isComplexType(Class<?> clazz) {
        return !clazz.isPrimitive() &&
                !clazz.getName().startsWith("java.") &&
                !clazz.isEnum() &&
                !clazz.isArray();
    }
}