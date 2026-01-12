package org.xcyms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.xcyms.common.ApiResult;
import org.xcyms.common.Constant;
import org.xcyms.entity.Config;
import org.xcyms.entity.dto.ConfigDTO;
import org.xcyms.mapper.ConfigMapper;
import org.xcyms.service.IConfigService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 系统/用户配置表 服务实现类
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-12
 */
@Service
@RequiredArgsConstructor
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, Config> implements IConfigService {

    private final ModelMapper modelMapper;

    @Override
    @Cacheable(value = Constant.Cache.CONFIG, key = "'val:' + (#userId == null ? 'sys' : #userId) + ':' + #key")
    public String getConfigValue(Long userId, String key) {
        // 1. 优先查用户定制配置
        if (userId != null) {
            Config userConfig = this.getOne(new LambdaQueryWrapper<Config>()
                    .eq(Config::getUserId, userId)
                    .eq(Config::getConfigKey, key));
            if (userConfig != null) {
                return userConfig.getConfigValue();
            }
        }

        // 2. 查系统默认配置 (userId 为 null)
        Config defaultConfig = this.getOne(new LambdaQueryWrapper<Config>()
                .isNull(Config::getUserId)
                .eq(Config::getConfigKey, key));

        return defaultConfig != null ? defaultConfig.getConfigValue() : null;
    }

    @Override
    @Cacheable(value = Constant.Cache.CONFIG, key = "'list:' + (#userId == null ? 'sys' : #userId)")
    public List<ConfigDTO> getUserConfigs(Long userId) {
        // 1. 获取所有系统默认配置
        List<Config> defaultConfigs = this.list(new LambdaQueryWrapper<Config>().isNull(Config::getUserId));

        if (userId == null) {
            return defaultConfigs.stream()
                    .map(c -> modelMapper.map(c, ConfigDTO.class))
                    .collect(Collectors.toList());
        }

        // 2. 获取该用户的定制配置
        List<Config> userConfigs = this.list(new LambdaQueryWrapper<Config>().eq(Config::getUserId, userId));
        Map<String, Config> userConfigMap = userConfigs.stream()
                .collect(Collectors.toMap(Config::getConfigKey, c -> c));

        // 3. 合并：用户配置覆盖默认配置
        return defaultConfigs.stream().map(dc -> {
            if (userConfigMap.containsKey(dc.getConfigKey())) {
                // 将用户配置的Config对象也转换为ConfigDTO对象
                return modelMapper.map(userConfigMap.get(dc.getConfigKey()), ConfigDTO.class);
            }
            return modelMapper.map(dc, ConfigDTO.class);
        }).collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = Constant.Cache.CONFIG, allEntries = true)
    public ApiResult<Boolean> updateConfig(ConfigDTO configDTO) {
        Config config = modelMapper.map(configDTO, Config.class);
        return ApiResult.success(this.updateById(config));
    }
}
