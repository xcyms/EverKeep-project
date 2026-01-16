package org.xcyms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xcyms.entity.Notice;

/**
 * <p>
 * 系统公告表 Mapper 接口
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-16
 */
@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {

}
