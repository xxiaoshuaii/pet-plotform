package com.pethub.service;

import com.pethub.pojo.dto.OrderCreateDTO;
import com.pethub.pojo.dto.OrderStatusDTO;
import com.pethub.pojo.query.OrderQuery;
import com.pethub.pojo.vo.OrderCreateVO;
import com.pethub.pojo.vo.OrderDetailVO;
import com.pethub.pojo.vo.OrderVO;
import com.pethub.pojo.vo.PageResultVO;

public interface OrderService {

    PageResultVO<OrderVO> page(OrderQuery query);

    OrderCreateVO create(Long userId, OrderCreateDTO orderCreateDTO);

    OrderDetailVO getById(Long id);

    void updateStatus(Long id, OrderStatusDTO orderStatusDTO);

    boolean removeById(Long id);
}
