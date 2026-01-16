package org.xcyms.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 *     系统公告DTO
 * </p>
 *
 * @author liu-xu
 * @date 2026年01月16日 9:50
 */
@Data
public class NoticeDTO {

    /**
     * ID
     */
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
     * 标签
     */
    private String tag;

    /**
     * 颜色
     */
    private String color;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
