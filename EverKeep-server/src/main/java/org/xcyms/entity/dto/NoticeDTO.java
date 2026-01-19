package org.xcyms.entity.dto;

import lombok.Data;
import org.xcyms.common.annotation.ApiDocProperty;

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

    @ApiDocProperty("公告ID")
    private Long id;

    @ApiDocProperty("公告标题")
    private String title;

    @ApiDocProperty("公告内容")
    private String content;

    @ApiDocProperty("标签文本")
    private String tag;

    @ApiDocProperty("标签颜色")
    private String color;

    @ApiDocProperty("显示排序")
    private Integer sort;

    @ApiDocProperty("发布时间")
    private LocalDateTime createTime;
}