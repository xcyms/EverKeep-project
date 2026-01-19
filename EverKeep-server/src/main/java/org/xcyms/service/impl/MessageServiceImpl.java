package org.xcyms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.xcyms.common.ApiResult;
import org.xcyms.common.enums.YesNoEnum;
import org.xcyms.entity.Message;
import org.xcyms.entity.dto.MessageDTO;
import org.xcyms.mapper.MessageMapper;
import org.xcyms.service.IMessageService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 系统消息表 服务实现类
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-16
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMessageService {

    private final ModelMapper modelMapper;

    @Override
    public ApiResult<Page<MessageDTO>> getMyPage(Page<Message> page, Long userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        // 查询属于该用户的消息，或者全体消息 (userId 为 null)
        wrapper.and(w -> w.eq(Message::getUserId, userId).or().isNull(Message::getUserId));
        wrapper.orderByDesc(Message::getCreateTime);

        Page<Message> messagePage = this.page(page, wrapper);
        Page<MessageDTO> dtoPage = new Page<>(messagePage.getCurrent(), messagePage.getSize(), messagePage.getTotal());

        List<MessageDTO> dtoList = messagePage.getRecords().stream()
                .map(m -> modelMapper.map(m, MessageDTO.class))
                .collect(Collectors.toList());

        dtoPage.setRecords(dtoList);
        return ApiResult.success(dtoPage);
    }

    @Override
    public ApiResult<Page<MessageDTO>> getAllPage(Page<Message> page, String title) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(title)) {
            wrapper.like(Message::getTitle, title);
        }
        wrapper.orderByDesc(Message::getCreateTime);

        Page<Message> messagePage = this.page(page, wrapper);
        Page<MessageDTO> dtoPage = new Page<>(messagePage.getCurrent(), messagePage.getSize(), messagePage.getTotal());

        List<MessageDTO> dtoList = messagePage.getRecords().stream()
                .map(m -> modelMapper.map(m, MessageDTO.class))
                .collect(Collectors.toList());

        dtoPage.setRecords(dtoList);
        return ApiResult.success(dtoPage);
    }

    @Override
    public ApiResult<String> readMessage(Long id, Long userId) {
        Message message = this.getById(id);
        if (message == null) {
            return ApiResult.error("消息不存在");
        }
        // 校验权限：只能读取全体消息或者发给自己的消息
        if (message.getUserId() != null && !message.getUserId().equals(userId)) {
            return ApiResult.error("无权操作此消息");
        }

        message.setReadFlag(YesNoEnum.YES);
        return this.updateById(message) ? ApiResult.success("标记成功") : ApiResult.error("操作失败");
    }

    @Override
    public ApiResult<String> readAllMessages(Long userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Message::getUserId, userId).or().isNull(Message::getUserId));
        wrapper.eq(Message::getReadFlag, YesNoEnum.NO);

        Message update = new Message();
        update.setReadFlag(YesNoEnum.YES);

        return this.update(update, wrapper) ? ApiResult.success("全部标记成功") : ApiResult.error("操作失败");
    }

    @Override
    public ApiResult<Long> getUnreadCount(Long userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Message::getUserId, userId).or().isNull(Message::getUserId));
        wrapper.eq(Message::getReadFlag, YesNoEnum.NO);
        return ApiResult.success(this.count(wrapper));
    }
}