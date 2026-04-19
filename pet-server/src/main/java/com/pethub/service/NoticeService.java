package com.pethub.service;

import com.pethub.pojo.entity.Orders;
import com.pethub.pojo.vo.NoticeVO;

import java.util.List;

public interface NoticeService {

    List<NoticeVO> list(Integer limit);

    Long getUnreadCount();

    void readById(Long id);

    void readAll();

    void syncOrderStatusNotice(Orders orders);
}
