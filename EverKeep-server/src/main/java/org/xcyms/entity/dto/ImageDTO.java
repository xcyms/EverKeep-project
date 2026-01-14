package org.xcyms.entity.dto;

import lombok.Data;
import org.xcyms.common.enums.YesNoEnum;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *     图片DTO
 * </p>
 * @author liu-xu
 * @date 2026年01月12日 9:22
 */
@Data
public class ImageDTO {

    private Long id;

    private Long userId;

    private Long albumId;

    private String url;

    private String name;

    private Long size;

    private String type;

    private YesNoEnum status;

    private LocalDateTime createTime;

    private List<Long> ids;
}
