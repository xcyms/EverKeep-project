package org.xcyms.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum EnableDisenableEnum {

    ENABLE(1, "可用"),
    DISABLE(0, "不可用");

    // @EnumValue 标记 MyBatis-Plus 存储到数据库的值
    @EnumValue
    // @JsonValue 标记 Jackson 序列化为 JSON 的值（前端收到的是 1 或 0）
    @JsonValue
    private final int code;

    private final String desc;

    EnableDisenableEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * @JsonCreator 标记 Jackson 反序列化逻辑
     * 当前端传 1 或 0 时，自动转为对应的枚举对象
     */
    @JsonCreator
    public static EnableDisenableEnum fromCode(int code) {
        for (EnableDisenableEnum item : EnableDisenableEnum.values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }
}
