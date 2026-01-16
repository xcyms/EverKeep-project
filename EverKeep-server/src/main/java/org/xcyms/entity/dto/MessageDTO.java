package org.xcyms.entity.dto;

import lombok.Data;
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

    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 类型: info, success, warning, error
     */
    private String type;

    /**
     * 是否已读
     */
    private YesNoEnum readFlag;

    /**
     * 所属用户ID (NULL表示全体消息)
     */
    private Long userId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
