package org.xcyms.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.xcyms.common.ApiResult;
import org.xcyms.common.annotation.ApiDoc;
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
@ApiDoc("公告管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {

    private final INoticeService noticeService;

    @ApiDoc("获取公告列表")
    @GetMapping("/list")
    public ApiResult<List<NoticeDTO>> getList() {
        return noticeService.getList();
    }

    @ApiDoc("分页查询公告列表")
    @GetMapping("/page")
    public ApiResult<IPage<NoticeDTO>> getPage(Page<Notice> page, @RequestParam(required = false) String title) {
        return noticeService.getPage(page, title);
    }

    @ApiDoc("发布新公告 (管理员)")
    @SaCheckRole("ADMIN")
    @PostMapping("/save")
    public ApiResult<String> save(@RequestBody Notice notice) {
        return noticeService.save(notice) ? ApiResult.success("发布成功") : ApiResult.error("发布失败");
    }

    @ApiDoc("更新公告 (管理员)")
    @SaCheckRole("ADMIN")
    @PostMapping("/update")
    public ApiResult<String> update(@RequestBody Notice notice) {
        return noticeService.updateById(notice) ? ApiResult.success("更新成功") : ApiResult.error("更新失败");
    }

    @ApiDoc("删除公告 (管理员)")
    @SaCheckRole("ADMIN")
    @DeleteMapping("/delete")
    public ApiResult<String> delete(@RequestParam Long id) {
        return noticeService.removeById(id) ? ApiResult.success("删除成功") : ApiResult.error("删除失败");
    }
}