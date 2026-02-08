package org.xcyms.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xcyms.common.ApiResult;
import org.xcyms.common.Constant;
import org.xcyms.entity.Video;
import org.xcyms.entity.dto.VideoDTO;
import org.xcyms.mapper.VideoMapper;
import org.xcyms.service.IConfigService;
import org.xcyms.service.IVideoService;
import org.xcyms.service.storage.StorageFactory;
import org.xcyms.utils.IdGenerator;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 视频表 服务实现类
 * </p>
 *
 * @author liu-xu
 * @since 2026-02-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements IVideoService {

    private final ModelMapper mapper;
    private final IConfigService configService;
    private final StorageFactory storageFactory;
    private final VideoProcessor videoProcessor;

    @Override
    public ApiResult<VideoDTO> uploadVideo(MultipartFile file, Long albumId, String category) {
        if (file == null || file.isEmpty()) {
            return ApiResult.error("文件不能为空");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)) {
            return ApiResult.error("文件名不能为空");
        }

        // 1. 验证 (大小、格式)
        ApiResult<VideoDTO> validateResult = validateFile(file, userId);
        if (validateResult.getCode() != 200) {
            return validateResult;
        }

        // 2. 路径处理
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        String relativePath = getRelativePath(userId, category);
        String newFileName = IdGenerator.nextIdStr() + suffix;
        String finalRelativePath = relativePath + newFileName;

        // 3. 临时保存并上传
        File tempFile = null;
        try {
            tempFile = File.createTempFile("upload_v_", suffix);
            file.transferTo(tempFile);

            // 执行上传策略
            String webUrl = storageFactory.getService().upload(tempFile, finalRelativePath);

            // 4. 数据入库
            Video video = new Video();
            video.setUserId(userId);
            video.setAlbumId(albumId);
            video.setName(originalFilename);
            video.setUrl(webUrl);
            video.setSize(file.getSize());
            video.setType(suffix.substring(1));

            this.save(video);

            // 5. 异步处理视频封面 (不再在 finally 中删除 tempFile，由 processor 处理)
            videoProcessor.processVideoAsync(tempFile, video.getId(), relativePath);

            return ApiResult.success(mapper.map(video, VideoDTO.class));

        } catch (IOException e) {
            log.error("视频上传失败", e);
            // 发生异常时清理临时文件
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
            return ApiResult.error("视频上传失败");
        }
    }

    private ApiResult<VideoDTO> validateFile(MultipartFile file, Long userId) {
        // 1. 校验用户存储空间配额
        String maxStorageStr = configService.getConfigValue(userId, Constant.ConfigKey.MAX_STORAGE_SIZE);
        long maxStorage = StringUtils.isNotBlank(maxStorageStr) ? Long.parseLong(maxStorageStr) : 100 * 1024 * 1024L;

        Map<String, Object> map = this.getMap(new QueryWrapper<Video>()
                .select("sum(size) as totalSize")
                .eq("user_id", userId)
                .eq("deleted", 0));
        long usedSize = map != null && map.get("totalSize") != null ? Long.parseLong(map.get("totalSize").toString()) : 0L;

        if (usedSize + file.getSize() > maxStorage) {
            return ApiResult.error("存储空间已不足");
        }

        // 2. 单文件大小校验 (视频允许大一点)
        long maxSize = 50 * 1024 * 1024; // 默认 50MB
        if (file.getSize() > maxSize) {
            return ApiResult.error("视频文件不能超过 50MB");
        }

        // 3. 格式校验
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        Set<String> allowedExtensions = Set.of("mp4", "mov", "avi", "mkv");
        String ext = suffix.startsWith(".") ? suffix.substring(1) : suffix;
        if (!allowedExtensions.contains(ext)) {
            return ApiResult.error("不支持的视频格式");
        }
        return ApiResult.success();
    }

    private String getRelativePath(Long userId, String category) {
        String userSubDir = configService.getConfigValue(userId, Constant.ConfigKey.USER_UPLOAD_DIR);
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(userSubDir)) {
            sb.append(userSubDir).append("/");
        }
        sb.append(category).append("/").append(datePath).append("/");
        return sb.toString();
    }

    @Override
    public ApiResult<IPage<VideoDTO>> getPage(Page<Video> page, VideoDTO videoDTO) {
        IPage<Video> iPage = this.page(page, new QueryWrapper<Video>()
                .lambda()
                .like(StringUtils.isNotBlank(videoDTO.getName()), Video::getName, videoDTO.getName())
                .eq(null != videoDTO.getStatus(), Video::getStatus, videoDTO.getStatus())
                .eq(null != videoDTO.getAlbumId(), Video::getAlbumId, videoDTO.getAlbumId())
                .eq(null != videoDTO.getUserId(), Video::getUserId, videoDTO.getUserId()));
        IPage<VideoDTO> iPageDTO = iPage.convert(video -> mapper.map(video, VideoDTO.class));
        return ApiResult.success(iPageDTO);
    }

    @Override
    public ApiResult<String> updateStatus(VideoDTO videoDTO) {
        LambdaQueryWrapper<Video> queryWrapper = new LambdaQueryWrapper<Video>()
                .in(Video::getId, videoDTO.getIds());
        Video video = new Video();
        video.setStatus(videoDTO.getStatus());
        this.update(video, queryWrapper);
        return ApiResult.success("状态更新成功");
    }

    @Override
    public ApiResult<String> move(Long videoId, Long albumId) {
        Video video = new Video();
        video.setAlbumId(albumId);
        this.update(video, new LambdaQueryWrapper<Video>().eq(Video::getId, videoId));
        return ApiResult.success("移动成功");
    }

    @Override
    public ApiResult<String> batchMove(VideoDTO videoDTO) {
        if (videoDTO.getIds() == null || videoDTO.getIds().isEmpty()) {
            return ApiResult.error("未选择视频");
        }
        LambdaQueryWrapper<Video> queryWrapper = new LambdaQueryWrapper<Video>()
                .in(Video::getId, videoDTO.getIds());
        Video video = new Video();
        video.setAlbumId(videoDTO.getAlbumId());
        this.update(video, queryWrapper);
        return ApiResult.success("批量移动成功");
    }

    @Override
    public ApiResult<IPage<VideoDTO>> getRecyclePage(Page<Video> page, Long userId) {
        IPage<Video> iPage = this.baseMapper.selectRecyclePage(page, userId);
        IPage<VideoDTO> iPageDTO = iPage.convert(video -> mapper.map(video, VideoDTO.class));
        return ApiResult.success(iPageDTO);
    }

    @Override
    public ApiResult<String> restore(List<Long> idList) {
        if (idList == null || idList.isEmpty()) {
            return ApiResult.error("请选择要恢复的视频");
        }
        String ids = StringUtils.join(idList, ",");
        this.baseMapper.restore(ids);
        return ApiResult.success("已从回收站恢复");
    }

    @Override
    public ApiResult<String> deletePermanently(List<Long> idList) {
        if (idList == null || idList.isEmpty()) {
            return ApiResult.error("请选择要删除的视频");
        }

        for (Long id : idList) {
            Video video = this.baseMapper.selectWithDeleted(id);
            if (video != null) {
                try {
                    storageFactory.getService().delete(video.getUrl());
                } catch (Exception e) {
                    log.error("删除物理文件失败: {}", video.getUrl(), e);
                }
                this.baseMapper.deletePermanently(id);
            }
        }
        return ApiResult.success("已永久删除");
    }
}