package org.xcyms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;
import org.xcyms.common.ApiResult;
import org.xcyms.entity.Video;
import org.xcyms.entity.dto.VideoDTO;

import java.util.List;

/**
 * <p>
 * 视频表 服务类
 * </p>
 *
 * @author liu-xu
 * @since 2026-02-08
 */
public interface IVideoService extends IService<Video> {

    ApiResult<VideoDTO> uploadVideo(MultipartFile file, Long albumId, String category);

    ApiResult<IPage<VideoDTO>> getPage(Page<Video> page, VideoDTO videoDTO);

    ApiResult<String> updateStatus(VideoDTO videoDTO);

    ApiResult<String> move(Long videoId, Long albumId);

    ApiResult<String> batchMove(VideoDTO videoDTO);

    /**
     * 获取回收站分页列表
     */
    ApiResult<IPage<VideoDTO>> getRecyclePage(Page<Video> page, Long userId);

    /**
     * 恢复视频
     */
    ApiResult<String> restore(List<Long> idList);

    /**
     * 彻底删除视频
     */
    ApiResult<String> deletePermanently(List<Long> idList);

    ApiResult<String> setCover(Long videoId);

    ApiResult<String> rename(VideoDTO videoDTO);
}