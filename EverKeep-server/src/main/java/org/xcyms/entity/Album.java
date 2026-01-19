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
 * 相册表
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-11
 */
@Getter
@Setter
@TableName("biz_album")
public class Album implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiDocProperty("相册ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiDocProperty("创建者用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiDocProperty("相册名称")
    @TableField("name")
    private String name;

    @ApiDocProperty("相册描述")
    @TableField("description")
    private String description;

    @ApiDocProperty("封面图地址")
    @TableField("cover")
    private String cover;

    @ApiDocProperty("创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiDocProperty("修改时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("deleted")
    @TableLogic
    private YesNoEnum deleted;
}