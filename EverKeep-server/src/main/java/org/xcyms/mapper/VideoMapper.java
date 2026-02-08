package org.xcyms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.xcyms.entity.Video;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 视频表 Mapper 接口
 * </p>
 *
 * @author liu-xu
 * @since 2026-02-08
 */
@Mapper
public interface VideoMapper extends BaseMapper<Video> {

    /**
     * 查询回收站分页数据 (忽略逻辑删除标志)
     */
    IPage<Video> selectRecyclePage(IPage<Video> page, @Param("userId") Long userId);

    /**
     * 物理删除
     */
    int deletePermanently(@Param("id") Long id);

    /**
     * 批量恢复
     */
    void restore(@Param("idList") String idList);

    /**
     * 查询回收站中超过指定时间的过期视频
     */
    List<Video> selectExpiredRecycleVideos(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 根据ID查询记录，忽略逻辑删除状态
     */
    Video selectWithDeleted(@Param("id") Long id);
}