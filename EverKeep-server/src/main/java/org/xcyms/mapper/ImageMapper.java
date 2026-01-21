package org.xcyms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.xcyms.entity.Image;


/**
 * <p>
 * 图片表 Mapper 接口
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-11
 */
@Mapper
public interface ImageMapper extends BaseMapper<Image> {

    /**
     * 查询回收站分页数据 (忽略逻辑删除标志)
     */
    IPage<Image> selectRecyclePage(IPage<Image> page, @Param("userId") Long userId);

    /**
     * 物理删除
     */
    int deletePermanently(@Param("id") Long id);

    /**
     * 批量恢复
     */
    void restore(@Param("idList") String idList);
}