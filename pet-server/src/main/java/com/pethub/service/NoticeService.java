package com.pethub.service;

import com.pethub.pojo.vo.NoticeVO;

import java.util.List;

public interface NoticeService {

    List<NoticeVO> list();

    Long getUnreadCount();

    void readById(Long id);

    void readAll();
}
