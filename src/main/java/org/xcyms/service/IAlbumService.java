package org.xcyms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xcyms.entity.Album;

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

    List<Album> getMyAlbums();

    boolean createAlbum(Album album);
}
