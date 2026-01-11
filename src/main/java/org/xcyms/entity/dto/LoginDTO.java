package org.xcyms.entity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author liu-xu
 * @date 2026年01月11日 15:33
 */
@Data
public class LoginDTO {

    @NotNull(message = "用户名不能为空")
    private String username;
    @NotNull(message = "密码不能为空")
    private String password;
    private String code;
}
