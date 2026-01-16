package org.xcyms.entity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 *     系统配置DTO
 * </p>
 * @author liu-xu
 * @date 2026年01月12日 11:06
 */
@Data
public class ConfigDTO {

    private Long id;

    /**
     * 关联用户ID (NULL表示系统默认配置)
     */
    private Long userId;

    /**
     * 配置键 (如: max_file_size, upload_path, allowed_extensions)
     */
    @NotNull(message = "配置键不能为空")
    private String configKey;

    /**
     * 配置值
     */
    @NotNull(message = "配置值不能为空")
    private String configValue;

    /**
     * 配置名称
     */
    @NotNull(message = "配置名称不能为空")
    private String configName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}