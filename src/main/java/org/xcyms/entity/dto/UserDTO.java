package org.xcyms.entity.dto;

import lombok.Data;
import org.xcyms.common.enums.EnableDisenableEnum;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *     用户DTO
 * </p>
 * @author liu-xu
 * @date 2026年01月12日 9:55
 */
@Data
public class UserDTO {

    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String avatar;

    private String email;

    private String phone;

    private EnableDisenableEnum status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<String> roles;
}
