package com.pethub.service.impl;

import com.pethub.common.exception.BusinessException;
import com.pethub.mapper.NoticeMapper;
import com.pethub.pojo.entity.Notice;
import com.pethub.pojo.vo.NoticeVO;
import com.pethub.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;

    @Override
    public List<NoticeVO> list() {
        return noticeMapper.selectList();
    }

    @Override
    public Long getUnreadCount() {
        Long count = noticeMapper.countUnread();
        return count == null ? 0L : count;
    }

    @Override
    public void readById(Long id) {
        Notice notice = noticeMapper.selectEntityById(id);
        if (notice == null) {
            throw new BusinessException("通知不存在");
        }

        int rows = noticeMapper.readById(id);
        if (rows < 1) {
            throw new BusinessException("标记已读失败");
        }
    }

    @Override
    public void readAll() {
        noticeMapper.readAll();
    }
}
