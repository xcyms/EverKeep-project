package org.xcyms.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.xcyms.common.ApiResult;
import org.xcyms.common.annotation.ApiDoc;
import org.xcyms.entity.Message;
import org.xcyms.entity.dto.MessageDTO;
import org.xcyms.service.IMessageService;

/**
 * <p>
 * 系统消息表 前端控制器
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-16
 */
@ApiDoc("消息管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/message")
public class MessageController {

    private final IMessageService messageService;

    @ApiDoc("获取我的消息分页列表")
    @GetMapping("/my/page")
    public ApiResult<Page<MessageDTO>> getMyPage(Page<Message> page) {
        return messageService.getMyPage(page, StpUtil.getLoginIdAsLong());
    }

    @ApiDoc("标记单条消息为已读")
    @PostMapping("/read")
    public ApiResult<String> read(Long id) {
        return messageService.readMessage(id, StpUtil.getLoginIdAsLong());
    }

    @ApiDoc("标记所有消息为已读")
    @PostMapping("/readAll")
    public ApiResult<String> readAll() {
        return messageService.readAllMessages(StpUtil.getLoginIdAsLong());
    }

    @ApiDoc("获取未读消息总数")
    @GetMapping("/unread/count")
    public ApiResult<Long> getUnreadCount() {
        return messageService.getUnreadCount(StpUtil.getLoginIdAsLong());
    }

    @ApiDoc("分页查询所有系统消息 (管理员)")
    @SaCheckRole("ADMIN")
    @GetMapping("/page")
    public ApiResult<Page<MessageDTO>> getAllPage(Page<Message> page, String title) {
        return messageService.getAllPage(page, title);
    }

    @ApiDoc("发布新系统消息 (管理员)")
    @SaCheckRole("ADMIN")
    @PostMapping("/save")
    public ApiResult<String> save(@RequestBody Message message) {
        return messageService.save(message) ? ApiResult.success("发布成功") : ApiResult.error("发布失败");
    }

    @ApiDoc("更新系统消息 (管理员)")
    @SaCheckRole("ADMIN")
    @PostMapping("/update")
    public ApiResult<String> update(@RequestBody Message message) {
        return messageService.updateById(message) ? ApiResult.success("更新成功") : ApiResult.error("更新失败");
    }

    @ApiDoc("删除系统消息 (管理员)")
    @SaCheckRole("ADMIN")
    @DeleteMapping("/delete")
    public ApiResult<String> delete(Long id) {
        return messageService.removeById(id) ? ApiResult.success("删除成功") : ApiResult.error("删除失败");
    }
}