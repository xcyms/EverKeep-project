package org.xcyms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.xcyms.common.ApiResult;
import org.xcyms.entity.Notice;
import org.xcyms.entity.dto.NoticeDTO;

import java.util.List;

/**
 * <p>
 * 系统公告表 服务类
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-16
 */
public interface INoticeService extends IService<Notice> {

    ApiResult<List<NoticeDTO>> getList();

    /**
     * 分页查询公告
     */
    ApiResult<IPage<NoticeDTO>> getPage(Page<Notice> page, String title);
}