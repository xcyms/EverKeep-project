package org.xcyms.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.xcyms.common.ApiResult;
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
@RestController
@RequiredArgsConstructor
@RequestMapping("/message")
public class MessageController {

    private final IMessageService messageService;

    /**
     * 分页查询我的消息
     */
    @GetMapping("/my/page")
    public ApiResult<Page<MessageDTO>> getMyPage(Page<Message> page) {
        return messageService.getMyPage(page, StpUtil.getLoginIdAsLong());
    }

    /**
     * 标记消息已读
     */
    @PostMapping("/read")
    public ApiResult<?> read(Long id) {
        return messageService.readMessage(id, StpUtil.getLoginIdAsLong());
    }

    /**
     * 全部标记为已读
     */
    @PostMapping("/readAll")
    public ApiResult<?> readAll() {
        return messageService.readAllMessages(StpUtil.getLoginIdAsLong());
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread/count")
    public ApiResult<Long> getUnreadCount() {
        return messageService.getUnreadCount(StpUtil.getLoginIdAsLong());
    }

    /**
     * 分页查询所有消息 (管理员)
     */
    @SaCheckRole("ADMIN")
    @GetMapping("/page")
    public ApiResult<Page<MessageDTO>> getAllPage(Page<Message> page, String title) {
        return messageService.getAllPage(page, title);
    }

    /**
     * 新增消息 (管理员)
     */
    @SaCheckRole("ADMIN")
    @PostMapping("/save")
    public ApiResult<?> save(@RequestBody Message message) {
        return messageService.save(message) ? ApiResult.success() : ApiResult.error("保存失败");
    }

    /**
     * 修改消息 (管理员)
     */
    @SaCheckRole("ADMIN")
    @PostMapping("/update")
    public ApiResult<?> update(@RequestBody Message message) {
        return messageService.updateById(message) ? ApiResult.success() : ApiResult.error("更新失败");
    }

    /**
     * 删除消息 (管理员)
     */
    @SaCheckRole("ADMIN")
    @DeleteMapping("/delete")
    public ApiResult<?> delete(Long id) {
        return messageService.removeById(id) ? ApiResult.success() : ApiResult.error("删除失败");
    }
}