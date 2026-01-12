package org.xcyms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-12
 */
@Getter
@Setter
@TableName("sys_user_role")
public class UserRole implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("user_id")
    private Long userId;

    @TableField("role_id")
    private Long roleId;
}
