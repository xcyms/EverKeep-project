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
 * 系统消息表
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-16
 */
@Getter
@Setter
@TableName("sys_message")
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiDocProperty("消息ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiDocProperty("消息标题")
    @TableField("title")
    private String title;

    @ApiDocProperty("消息内容")
    @TableField("content")
    private String content;

    @ApiDocProperty("消息类型 (info, success, warning, error)")
    @TableField("type")
    private String type;

    @ApiDocProperty("已读标志 (YES-已读, NO-未读)")
    @TableField("read_flag")
    private YesNoEnum readFlag;

    @ApiDocProperty("目标用户ID (NULL为全体)")
    @TableField("user_id")
    private Long userId;

    @ApiDocProperty("创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField("deleted")
    @TableLogic
    private YesNoEnum deleted;
}