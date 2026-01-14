package org.xcyms.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 启用禁用枚举
 */
@Getter
public enum EnableDisenableEnum implements BaseEnum {

    ENABLE(1, "可用"),
    DISABLE(0, "不可用");

    @EnumValue
    private final Integer code;
    private final String desc;

    EnableDisenableEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}