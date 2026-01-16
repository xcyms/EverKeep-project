package org.xcyms.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.xcyms.entity.Album;
import org.xcyms.entity.dto.AlbumDTO;

/**
 * <p>
 * 相册表 Mapper 接口
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-11
 */
@Mapper
public interface AlbumMapper extends BaseMapper<Album> {

    IPage<AlbumDTO> selectAlbumPageWithImageCount(IPage<Album> page, @Param(Constants.WRAPPER) Wrapper<Album> queryWrapper);

}