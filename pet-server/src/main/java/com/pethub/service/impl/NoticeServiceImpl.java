package com.pethub.service.impl;

import com.pethub.common.cache.DashboardCacheNames;
import com.pethub.common.exception.BusinessException;
import com.pethub.mapper.NoticeMapper;
import com.pethub.pojo.entity.Notice;
import com.pethub.pojo.entity.Orders;
import com.pethub.pojo.vo.NoticeVO;
import com.pethub.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;

    @Override
    public List<NoticeVO> list(Integer limit) {
        return noticeMapper.selectList(limit);
    }

    @Override
    public Long getUnreadCount() {
        Long count = noticeMapper.countUnread();
        return count == null ? 0L : count;
    }

    @Override
    @CacheEvict(cacheNames = DashboardCacheNames.NOTICES, allEntries = true)
    public void readById(Long id) {
        Notice notice = noticeMapper.selectEntityById(id);
        if (notice == null) {
            throw new BusinessException("Notice does not exist");
        }

        int rows = noticeMapper.readById(id);
        if (rows < 1) {
            throw new BusinessException("Failed to mark notice as read");
        }
    }

    @Override
    @CacheEvict(cacheNames = DashboardCacheNames.NOTICES, allEntries = true)
    public void readAll() {
        noticeMapper.readAll();
    }

    @Override
    @CacheEvict(cacheNames = DashboardCacheNames.NOTICES, allEntries = true)
    public void syncOrderStatusNotice(Orders orders) {
        if (orders == null || orders.getId() == null || orders.getStatus() == null) {
            return;
        }

        noticeMapper.softDeleteByOrderId(orders.getId());

        Notice notice = new Notice();
        notice.setType(resolveNoticeType(orders.getStatus()));
        notice.setTitle(resolveNoticeTitle(orders.getStatus()));
        notice.setContent(resolveNoticeContent(orders.getOrderNo(), orders.getStatus()));
        notice.setIsRead(0);
        notice.setOrderId(orders.getId());
        noticeMapper.insert(notice);
    }

    private String resolveNoticeType(Integer status) {
        return switch (status) {
            case 0 -> "new_order";
            case 1 -> "deliver";
            case 2 -> "completed";
            case 3 -> "cancelled";
            case 4 -> "refund";
            default -> "order_status";
        };
    }

    private String resolveNoticeTitle(Integer status) {
        return switch (status) {
            case 0 -> "订单待支付";
            case 1 -> "订单待处理";
            case 2 -> "订单已完成";
            case 3 -> "订单已取消";
            case 4 -> "订单已退款";
            default -> "订单状态已更新";
        };
    }

    private String resolveNoticeContent(String orderNo, Integer status) {
        String displayOrderNo = orderNo == null || orderNo.isBlank() ? "-" : orderNo;
        String statusText = switch (status) {
            case 0 -> "待支付";
            case 1 -> "待处理";
            case 2 -> "已完成";
            case 3 -> "已取消";
            case 4 -> "已退款";
            default -> "已更新";
        };
        return "订单 " + displayOrderNo + " 当前状态为 " + statusText;
    }
}
