package org.xcyms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import org.xcyms.common.annotation.ApiDocProperty;
import org.xcyms.common.enums.YesNoEnum;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 视频表
 * </p>
 *
 * @author liu-xu
 * @since 2026-02-08
 */
@Getter
@Setter
@TableName("biz_video")
public class Video implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiDocProperty("视频ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiDocProperty("用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiDocProperty("相册ID")
    @TableField("album_id")
    private Long albumId;

    @ApiDocProperty("视频URL")
    @TableField("url")
    private String url;

    @ApiDocProperty("封面图URL")
    @TableField("cover_url")
    private String coverUrl;

    @ApiDocProperty("视频名称")
    @TableField("name")
    private String name;

    @ApiDocProperty("文件大小(Byte)")
    @TableField("size")
    private Long size;

    @ApiDocProperty("文件类型")
    @TableField("type")
    private String type;

    @ApiDocProperty("时长(秒)")
    @TableField("duration")
    private Long duration;

    @ApiDocProperty("状态 (YES-发布, NO-私有)")
    @TableField("status")
    private YesNoEnum status;

    @ApiDocProperty("创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiDocProperty("更新时间")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @ApiDocProperty("删除标志 (YES-已删除, NO-未删除)")
    @TableField("deleted")
    @TableLogic
    private YesNoEnum deleted;
}