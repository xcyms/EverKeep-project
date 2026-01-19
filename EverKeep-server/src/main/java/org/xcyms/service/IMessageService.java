package org.xcyms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.xcyms.common.ApiResult;
import org.xcyms.entity.Message;
import org.xcyms.entity.dto.MessageDTO;

/**
 * <p>
 * 系统消息表 服务类
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-16
 */
public interface IMessageService extends IService<Message> {

    /**
     * 分页查询我的消息
     * @param page 分页参数
     * @param userId 用户ID
     * @return 分页结果
     */
    ApiResult<Page<MessageDTO>> getMyPage(Page<Message> page, Long userId);

    /**
     * 分页查询所有消息 (管理员)
     * @param page 分页参数
     * @param title 标题模糊查询
     * @return 分页结果
     */
    ApiResult<Page<MessageDTO>> getAllPage(Page<Message> page, String title);

    /**
     * 标记消息已读
     * @param id 消息ID
     * @param userId 用户ID
     * @return 结果
     */
    ApiResult<String> readMessage(Long id, Long userId);

    /**
     * 全部标记为已读
     * @param userId 用户ID
     * @return 结果
     */
    ApiResult<String> readAllMessages(Long userId);

    /**
     * 获取未读消息数量
     * @param userId 用户ID
     * @return 数量
     */
    ApiResult<Long> getUnreadCount(Long userId);
}