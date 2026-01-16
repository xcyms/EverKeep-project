package org.xcyms.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.xcyms.common.ApiResult;
import org.xcyms.entity.Notice;
import org.xcyms.entity.dto.NoticeDTO;
import org.xcyms.service.INoticeService;

import java.util.List;

/**
 * <p>
 * 系统公告表 前端控制器
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-16
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {

    private final INoticeService noticeService;

    @GetMapping("/list")
    public ApiResult<List<NoticeDTO>> getList() {
        return noticeService.getList();
    }

    @GetMapping("/page")
    public ApiResult<?> getPage(Page<Notice> page, @RequestParam(required = false) String title) {
        return noticeService.getPage(page, title);
    }

    @SaCheckRole("ADMIN")
    @PostMapping("/save")
    public ApiResult<?> save(@RequestBody Notice notice) {
        return noticeService.save(notice) ? ApiResult.success() : ApiResult.error("保存失败");
    }

    @SaCheckRole("ADMIN")
    @PostMapping("/update")
    public ApiResult<?> update(@RequestBody Notice notice) {
        return noticeService.updateById(notice) ? ApiResult.success() : ApiResult.error("更新失败");
    }

    @SaCheckRole("ADMIN")
    @DeleteMapping("/delete")
    public ApiResult<?> delete(@RequestParam Long id) {
        return noticeService.removeById(id) ? ApiResult.success() : ApiResult.error("删除失败");
    }
}