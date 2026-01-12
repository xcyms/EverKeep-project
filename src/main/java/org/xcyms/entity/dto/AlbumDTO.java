package org.xcyms.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author liu-xu
 * @date 2026年01月12日 10:38
 */
@Data
public class AlbumDTO {

    private Long id;

    private Long userId;

    private String name;

    private String description;

    private String cover;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
