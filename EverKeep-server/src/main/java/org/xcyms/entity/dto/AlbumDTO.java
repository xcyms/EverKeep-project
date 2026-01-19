package org.xcyms.entity.dto;

import lombok.Data;
import org.xcyms.common.annotation.ApiDocProperty;

import java.time.LocalDateTime;

/**
 * <p>
 *     相册DTO
 * </p>
 * @author liu-xu
 * @date 2026年01月12日 10:38
 */
@Data
public class AlbumDTO {

    @ApiDocProperty("主键ID")
    private Long id;

    @ApiDocProperty("用户ID")
    private Long userId;

    @ApiDocProperty("相册名称")
    private String name;

    @ApiDocProperty("相册描述")
    private String description;

    @ApiDocProperty("封面图地址")
    private String cover;

    @ApiDocProperty("图片总数")
    private Long imageCount;

    @ApiDocProperty("创建时间")
    private LocalDateTime createTime;

    @ApiDocProperty("修改时间")
    private LocalDateTime updateTime;
}