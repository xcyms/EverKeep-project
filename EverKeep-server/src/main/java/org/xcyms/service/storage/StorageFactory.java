package org.xcyms.service.storage;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.xcyms.common.Constant;
import org.xcyms.service.IConfigService;

import java.util.Map;

/**
 * 存储策略工厂
 */
@Component
@RequiredArgsConstructor
public class StorageFactory {

    private final Map<String, IStorageService> storageServiceMap;
    private final IConfigService configService;

    public IStorageService getService() {
        // 从数据库配置表读取当前存储类型 (LOCAL/S3)
        String type = configService.getConfigValue(null, Constant.ConfigKey.STORAGE_TYPE);
        if (StringUtils.isBlank(type)) {
            return storageServiceMap.get("LOCAL");
        }
        IStorageService service = storageServiceMap.get(type.toUpperCase());
        return service != null ? service : storageServiceMap.get("LOCAL");
    }
}