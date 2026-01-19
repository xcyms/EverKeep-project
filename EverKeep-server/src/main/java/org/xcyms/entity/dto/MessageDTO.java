package org.xcyms.entity.dto;

import lombok.Data;
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
@Data
public class MessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiDocProperty("消息ID")
    private Long id;

    @ApiDocProperty("消息标题")
    private String title;

    @ApiDocProperty("消息内容")
    private String content;

    @ApiDocProperty("消息类型 (info, success, warning, error)")
    private String type;

    @ApiDocProperty("已读标志 (YES-已读, NO-未读)")
    private YesNoEnum readFlag;

    @ApiDocProperty("目标用户ID (NULL为全体)")
    private Long userId;

    @ApiDocProperty("创建时间")
    private LocalDateTime createTime;
}