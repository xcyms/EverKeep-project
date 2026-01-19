package org.xcyms.entity.dto;

import lombok.Data;
import org.xcyms.common.annotation.ApiDocProperty;
import org.xcyms.common.enums.YesNoEnum;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *     图片DTO
 * </p>
 * @author liu-xu
 * @date 2026年01月12日 9:22
 */
@Data
public class ImageDTO {

    @ApiDocProperty("主键ID")
    private Long id;

    @ApiDocProperty("用户ID")
    private Long userId;

    @ApiDocProperty("相册ID")
    private Long albumId;

    @ApiDocProperty("图片URL")
    private String url;

    @ApiDocProperty("图片名称")
    private String name;

    @ApiDocProperty("文件大小(Byte)")
    private Long size;

    @ApiDocProperty("文件后缀类型")
    private String type;

    @ApiDocProperty("公开状态(YES-公开, NO-私有)")
    private YesNoEnum status;

    @ApiDocProperty("上传时间")
    private LocalDateTime createTime;

    @ApiDocProperty("批量操作ID列表")
    private List<Long> ids;
}