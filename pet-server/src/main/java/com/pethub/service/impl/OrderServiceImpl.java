package com.pethub.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pethub.common.exception.BusinessException;
import com.pethub.mapper.OrderMapper;
import com.pethub.mapper.PetMapper;
import com.pethub.mapper.UserMapper;
import com.pethub.pojo.dto.OrderCreateDTO;
import com.pethub.pojo.dto.OrderStatusDTO;
import com.pethub.pojo.entity.Orders;
import com.pethub.pojo.entity.Pet;
import com.pethub.pojo.query.OrderQuery;
import com.pethub.pojo.vo.OrderCreateVO;
import com.pethub.pojo.vo.OrderDetailVO;
import com.pethub.pojo.vo.OrderVO;
import com.pethub.pojo.vo.PageResultVO;
import com.pethub.pojo.vo.UserDetailVO;
import com.pethub.service.NoticeService;
import com.pethub.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final PetMapper petMapper;
    private final UserMapper userMapper;
    private final NoticeService noticeService;

    @Override
    public PageResultVO<OrderVO> page(OrderQuery query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();

        PageHelper.startPage(pageNum, pageSize);
        List<OrderVO> records = orderMapper.selectPage(query);
        PageInfo<OrderVO> pageInfo = new PageInfo<>(records);

        return new PageResultVO<>(pageInfo.getList(), pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateVO create(Long userId, OrderCreateDTO orderCreateDTO) {
        validateCreateOrder(userId, orderCreateDTO);

        UserDetailVO userDetailVO = userMapper.selectById(userId);
        if (userDetailVO == null) {
            throw new BusinessException("用户不存在");
        }
        if (userDetailVO.getStatus() != null && userDetailVO.getStatus() == 0) {
            throw new BusinessException("当前用户已被禁用，无法下单");
        }

        Pet pet = petMapper.selectEntityById(orderCreateDTO.getPetId());
        if (pet == null) {
            throw new BusinessException("宠物不存在");
        }
        if (pet.getStatus() == null || pet.getStatus() != 1) {
            throw new BusinessException("当前宠物暂不可下单");
        }
        if (pet.getStock() == null || pet.getStock() < 1) {
            throw new BusinessException("当前宠物库存不足");
        }

        int stockRows = petMapper.decreaseStockById(pet.getId());
        if (stockRows < 1) {
            throw new BusinessException("当前宠物库存不足，请刷新后重试");
        }

        Orders orders = new Orders();
        orders.setOrderNo(generateOrderNo());
        orders.setUserId(userId);
        orders.setPetId(pet.getId());
        orders.setAmount(pet.getPrice());
        orders.setStatus(0);
        orders.setContactName(orderCreateDTO.getContactName().trim());
        orders.setContactPhone(orderCreateDTO.getContactPhone().trim());
        orders.setAddress(orderCreateDTO.getAddress().trim());
        orders.setRemark(orderCreateDTO.getRemark() == null ? null : orderCreateDTO.getRemark().trim());

        int rows = orderMapper.insert(orders);
        if (rows < 1) {
            throw new BusinessException("创建订单失败");
        }

        noticeService.syncOrderStatusNotice(orders);
        return new OrderCreateVO(orders.getId(), orders.getOrderNo());
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
    @Transactional(rollbackFor = Exception.class)
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

        if (shouldRestoreStock(orders.getStatus(), orderStatusDTO.getStatus())) {
            int stockRows = petMapper.increaseStockById(orders.getPetId());
            if (stockRows < 1) {
                throw new BusinessException("回补宠物库存失败");
            }
        }

        int rows = orderMapper.updateStatusById(id, orderStatusDTO.getStatus());
        if (rows < 1) {
            throw new BusinessException("更新订单状态失败");
        }

        orders.setStatus(orderStatusDTO.getStatus());
        noticeService.syncOrderStatusNotice(orders);
    }

    @Override
    public boolean removeById(Long id) {
        Orders orders = orderMapper.selectEntityById(id);
        if (orders == null) {
            throw new BusinessException("订单不存在");
        }
        return orderMapper.deleteById(id) > 0;
    }

    private void validateCreateOrder(Long userId, OrderCreateDTO orderCreateDTO) {
        if (userId == null) {
            throw new BusinessException("未登录或登录已失效");
        }
        if (orderCreateDTO == null) {
            throw new BusinessException("下单参数不能为空");
        }
        if (orderCreateDTO.getPetId() == null) {
            throw new BusinessException("宠物 ID 不能为空");
        }
        if (orderCreateDTO.getContactName() == null || orderCreateDTO.getContactName().isBlank()) {
            throw new BusinessException("联系人不能为空");
        }
        if (orderCreateDTO.getContactPhone() == null || orderCreateDTO.getContactPhone().isBlank()) {
            throw new BusinessException("联系电话不能为空");
        }
        if (orderCreateDTO.getAddress() == null || orderCreateDTO.getAddress().isBlank()) {
            throw new BusinessException("联系地址不能为空");
        }
    }

    private boolean shouldRestoreStock(Integer oldStatus, Integer newStatus) {
        boolean toCanceledOrRefunded = newStatus != null && (newStatus == 3 || newStatus == 4);
        boolean alreadyCanceledOrRefunded = oldStatus != null && (oldStatus == 3 || oldStatus == 4);
        return toCanceledOrRefunded && !alreadyCanceledOrRefunded;
    }

    private String generateOrderNo() {
        return "PETU" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }
}
