package org.xcyms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import org.xcyms.common.enums.YesNoEnum;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 图片表
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-11
 */
@Getter
@Setter
@TableName("biz_image")
public class Image implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("album_id")
    private Long albumId;

    @TableField("url")
    private String url;

    @TableField("name")
    private String name;

    @TableField("size")
    private Long size;

    @TableField("type")
    private String type;

    @TableField("status")
    private YesNoEnum status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField("deleted")
    @TableLogic
    private YesNoEnum deleted;
}
