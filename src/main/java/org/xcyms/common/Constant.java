package org.xcyms.common;

import lombok.Getter;

/**
 * @author liu-xu
 * @date 2026年01月11日 15:31
 */
@Getter
public class Constant {

    public static final String SALT = "everKeep_salt";

    /**
     * 缓存常量
     */
    public interface Cache {
        String CONFIG = "config";
    }

    /**
     * 角色常量
     */
    public interface Role {
        Long ADMIN = 1L;
        Long USER = 2L;
    }
}
