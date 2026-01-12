package org.xcyms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xcyms.common.ApiResult;
import org.xcyms.entity.Album;
import org.xcyms.entity.dto.AlbumDTO;

import java.util.List;

/**
 * <p>
 * 相册表 服务类
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-11
 */
public interface IAlbumService extends IService<Album> {

    ApiResult<List<AlbumDTO>> getMyAlbums();

    boolean createAlbum(AlbumDTO albumDTO);

    ApiResult<List<AlbumDTO>> getMyAlbums(String name, Long userId);
}
