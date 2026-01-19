package org.xcyms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.xcyms.common.ApiResult;
import org.xcyms.entity.Notice;
import org.xcyms.entity.dto.NoticeDTO;
import org.xcyms.mapper.NoticeMapper;
import org.xcyms.service.INoticeService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 系统公告表 服务实现类
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-16
 */
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements INoticeService {

    private final ModelMapper modelMapper;

    @Override
    public ApiResult<List<NoticeDTO>> getList() {
        List<Notice> list = list(new QueryWrapper<Notice>()
                .lambda()
                .orderByAsc(Notice::getSort)
                .orderByDesc(Notice::getCreateTime)
                .last("LIMIT 5"));

        // 将Notice实体转换为NoticeDTO
        List<NoticeDTO> dtoList = list.stream()
                .map(n -> modelMapper.map(n, NoticeDTO.class))
                .collect(Collectors.toList());

        return ApiResult.success(dtoList);
    }

    @Override
    public ApiResult<IPage<NoticeDTO>> getPage(Page<Notice> page, String title) {
        IPage<Notice> result = this.page(page, new QueryWrapper<Notice>()
                .lambda()
                .like(StringUtils.isNotBlank(title), Notice::getTitle, title)
                .orderByAsc(Notice::getSort)
                .orderByDesc(Notice::getCreateTime));
        IPage<NoticeDTO> dtoResult = result.convert(n -> modelMapper.map(n, NoticeDTO.class));
        return ApiResult.success(dtoResult);
    }
}