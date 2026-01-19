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
 * 系统公告表
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-16
 */
@Getter
@Setter
@TableName("sys_notice")
public class Notice implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiDocProperty("公告ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiDocProperty("公告标题")
    @TableField("title")
    private String title;

    @ApiDocProperty("公告内容")
    @TableField("content")
    private String content;

    @ApiDocProperty("标签文本")
    @TableField("tag")
    private String tag;

    @ApiDocProperty("标签颜色")
    @TableField("color")
    private String color;

    @ApiDocProperty("显示排序")
    @TableField("sort")
    private Integer sort;

    @ApiDocProperty("发布时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 逻辑删除
     */
    @TableField("deleted")
    @TableLogic
    private YesNoEnum deleted;
}