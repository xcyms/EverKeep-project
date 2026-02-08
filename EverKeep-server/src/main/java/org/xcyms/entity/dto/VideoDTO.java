package org.xcyms.entity.dto;

import lombok.Data;
import org.xcyms.common.annotation.ApiDocProperty;
import org.xcyms.common.enums.YesNoEnum;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *     视频DTO
 * </p>
 * @author liu-xu
 * @date 2026年02月08日
 */
@Data
public class VideoDTO {

    @ApiDocProperty("主键ID")
    private Long id;

    @ApiDocProperty("用户ID")
    private Long userId;

    @ApiDocProperty("相册ID")
    private Long albumId;

    @ApiDocProperty("视频URL")
    private String url;

    @ApiDocProperty("封面图URL")
    private String coverUrl;

    @ApiDocProperty("视频名称")
    private String name;

    @ApiDocProperty("文件大小(Byte)")
    private Long size;

    @ApiDocProperty("文件后缀类型")
    private String type;

    @ApiDocProperty("时长(秒)")
    private Long duration;

    @ApiDocProperty("公开状态(YES-公开, NO-私有)")
    private YesNoEnum status;

    @ApiDocProperty("上传时间")
    private LocalDateTime createTime;

    @ApiDocProperty("更新时间")
    private LocalDateTime updateTime;

    @ApiDocProperty("批量操作ID列表")
    private List<Long> ids;
}