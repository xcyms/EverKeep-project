package org.xcyms.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.xcyms.entity.Album;
import org.xcyms.mapper.AlbumMapper;
import org.xcyms.service.IAlbumService;

import java.util.List;

/**
 * <p>
 * 相册表 服务实现类
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-11
 */
@Service
public class AlbumServiceImpl extends ServiceImpl<AlbumMapper, Album> implements IAlbumService {

    @Override
    public List<Album> getMyAlbums() {
        Long userId = StpUtil.getLoginIdAsLong();
        return baseMapper.selectList(new LambdaQueryWrapper<Album>()
                .eq(Album::getUserId, userId)
                .orderByDesc(Album::getCreateTime));
    }

    @Override
    public boolean createAlbum(Album album) {
        album.setUserId(StpUtil.getLoginIdAsLong());
        return baseMapper.insert(album) > 0;
    }
}
