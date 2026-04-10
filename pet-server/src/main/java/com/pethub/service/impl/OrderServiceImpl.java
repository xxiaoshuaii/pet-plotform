package com.pethub.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pethub.common.exception.BusinessException;
import com.pethub.mapper.OrderMapper;
import com.pethub.pojo.dto.OrderStatusDTO;
import com.pethub.pojo.entity.Orders;
import com.pethub.pojo.query.OrderQuery;
import com.pethub.pojo.vo.OrderDetailVO;
import com.pethub.pojo.vo.OrderVO;
import com.pethub.pojo.vo.PageResultVO;
import com.pethub.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;

    @Override
    public PageResultVO<OrderVO> page(OrderQuery query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();

        PageHelper.startPage(pageNum, pageSize);
        List<OrderVO> records = orderMapper.selectPage(query);
        PageInfo<OrderVO> pageInfo = new PageInfo<>(records);

        return new PageResultVO<>(
                pageInfo.getList(),
                pageInfo.getTotal(),
                pageInfo.getPageNum(),
                pageInfo.getPageSize()
        );
    }

    @Override
    public OrderDetailVO getById(Long id) {
        OrderDetailVO orderDetailVO = orderMapper.selectDetailById(id);
        if (orderDetailVO == null) {
            throw new BusinessException("订单不存在");
        }
        return orderDetailVO;
    }

    @Override
    public void updateStatus(Long id, OrderStatusDTO orderStatusDTO) {
        if (orderStatusDTO.getStatus() == null) {
            throw new BusinessException("订单状态不能为空");
        }
        if (orderStatusDTO.getStatus() < 0 || orderStatusDTO.getStatus() > 4) {
            throw new BusinessException("订单状态不合法");
        }

        Orders orders = orderMapper.selectEntityById(id);
        if (orders == null) {
            throw new BusinessException("订单不存在");
        }

        int rows = orderMapper.updateStatusById(id, orderStatusDTO.getStatus());
        if (rows < 1) {
            throw new BusinessException("更新订单状态失败");
        }
    }

    @Override
    public boolean removeById(Long id) {
        Orders orders = orderMapper.selectEntityById(id);
        if (orders == null) {
            throw new BusinessException("订单不存在");
        }
        return orderMapper.deleteById(id) > 0;
    }
}
