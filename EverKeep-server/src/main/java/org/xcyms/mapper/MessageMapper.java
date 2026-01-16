package org.xcyms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xcyms.entity.Message;

/**
 * <p>
 * 系统消息表 Mapper 接口
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-16
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

}
