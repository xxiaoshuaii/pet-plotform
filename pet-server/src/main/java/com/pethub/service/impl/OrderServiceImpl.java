package com.pethub.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pethub.common.cache.DashboardCacheNames;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private final StringRedisTemplate redisTemplate;

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
    @Caching(evict = {
            @CacheEvict(cacheNames = DashboardCacheNames.OVERVIEW, allEntries = true),
            @CacheEvict(cacheNames = DashboardCacheNames.ORDER_TREND, allEntries = true),
            @CacheEvict(cacheNames = DashboardCacheNames.RECENT_ORDERS, allEntries = true),
            @CacheEvict(cacheNames = DashboardCacheNames.NOTICES, allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateVO create(Long userId, OrderCreateDTO orderCreateDTO) {
        validateCreateOrder(userId, orderCreateDTO);

        UserDetailVO userDetailVO = userMapper.selectById(userId);
        if (userDetailVO == null) {
            throw new BusinessException("User does not exist");
        }
        if (userDetailVO.getStatus() != null && userDetailVO.getStatus() == 0) {
            throw new BusinessException("Current user is disabled");
        }

        Pet pet = petMapper.selectEntityById(orderCreateDTO.getPetId());
        if (pet == null) {
            throw new BusinessException("Pet does not exist");
        }
        if (pet.getStatus() == null || pet.getStatus() != 1) {
            throw new BusinessException("Current pet is not available for ordering");
        }
        if (pet.getStock() == null || pet.getStock() < 1) {
            throw new BusinessException("Insufficient pet stock");
        }

        int stockRows = petMapper.decreaseStockById(pet.getId());
        if (stockRows < 1) {
            throw new BusinessException("Insufficient pet stock, please refresh and try again");
        }
        evictPetCache(pet.getId());

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
            throw new BusinessException("Failed to create order");
        }

        noticeService.syncOrderStatusNotice(orders);
        return new OrderCreateVO(orders.getId(), orders.getOrderNo());
    }

    @Override
    public OrderDetailVO getById(Long id) {
        OrderDetailVO orderDetailVO = orderMapper.selectDetailById(id);
        if (orderDetailVO == null) {
            throw new BusinessException("Order does not exist");
        }
        return orderDetailVO;
    }

    @Override
    public OrderDetailVO getByIdForUser(Long userId, Long id) {
        Orders orders = orderMapper.selectEntityById(id);
        if (orders == null) {
            throw new BusinessException("Order does not exist");
        }
        if (!orders.getUserId().equals(userId)) {
            throw new BusinessException("No permission to view this order");
        }
        return getById(id);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = DashboardCacheNames.OVERVIEW, allEntries = true),
            @CacheEvict(cacheNames = DashboardCacheNames.RECENT_ORDERS, allEntries = true),
            @CacheEvict(cacheNames = DashboardCacheNames.NOTICES, allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)
    public void pay(Long userId, Long id) {
        Orders orders = orderMapper.selectEntityById(id);
        if (orders == null) {
            throw new BusinessException("Order does not exist");
        }
        if (!orders.getUserId().equals(userId)) {
            throw new BusinessException("No permission to pay this order");
        }
        if (orders.getStatus() == null || orders.getStatus() != 0) {
            throw new BusinessException("Current order status does not allow payment");
        }

        int rows = orderMapper.updateStatusById(id, 1);
        if (rows < 1) {
            throw new BusinessException("Failed to pay order");
        }

        orders.setStatus(1);
        noticeService.syncOrderStatusNotice(orders);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = DashboardCacheNames.OVERVIEW, allEntries = true),
            @CacheEvict(cacheNames = DashboardCacheNames.ORDER_TREND, allEntries = true),
            @CacheEvict(cacheNames = DashboardCacheNames.RECENT_ORDERS, allEntries = true),
            @CacheEvict(cacheNames = DashboardCacheNames.NOTICES, allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)
    public void cancelForUser(Long userId, Long id) {
        Orders orders = orderMapper.selectEntityById(id);
        if (orders == null) {
            throw new BusinessException("Order does not exist");
        }
        if (!orders.getUserId().equals(userId)) {
            throw new BusinessException("No permission to cancel this order");
        }
        if (orders.getStatus() == null || (orders.getStatus() != 0 && orders.getStatus() != 1)) {
            throw new BusinessException("Current order status does not allow cancellation");
        }

        updateStatusInternal(id, new OrderStatusDTO(null, 3));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = DashboardCacheNames.OVERVIEW, allEntries = true),
            @CacheEvict(cacheNames = DashboardCacheNames.ORDER_TREND, allEntries = true),
            @CacheEvict(cacheNames = DashboardCacheNames.RECENT_ORDERS, allEntries = true),
            @CacheEvict(cacheNames = DashboardCacheNames.NOTICES, allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, OrderStatusDTO orderStatusDTO) {
        updateStatusInternal(id, orderStatusDTO);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = DashboardCacheNames.OVERVIEW, allEntries = true),
            @CacheEvict(cacheNames = DashboardCacheNames.ORDER_TREND, allEntries = true),
            @CacheEvict(cacheNames = DashboardCacheNames.RECENT_ORDERS, allEntries = true),
            @CacheEvict(cacheNames = DashboardCacheNames.NOTICES, allEntries = true)
    })
    public boolean removeById(Long id) {
        Orders orders = orderMapper.selectEntityById(id);
        if (orders == null) {
            throw new BusinessException("Order does not exist");
        }
        return orderMapper.deleteById(id) > 0;
    }

    private void updateStatusInternal(Long id, OrderStatusDTO orderStatusDTO) {
        if (orderStatusDTO.getStatus() == null) {
            throw new BusinessException("Order status cannot be empty");
        }
        if (orderStatusDTO.getStatus() < 0 || orderStatusDTO.getStatus() > 4) {
            throw new BusinessException("Illegal order status");
        }

        Orders orders = orderMapper.selectEntityById(id);
        if (orders == null) {
            throw new BusinessException("Order does not exist");
        }

        if (shouldRestoreStock(orders.getStatus(), orderStatusDTO.getStatus())) {
            int stockRows = petMapper.increaseStockById(orders.getPetId());
            if (stockRows < 1) {
                throw new BusinessException("Failed to restore pet stock");
            }
            evictPetCache(orders.getPetId());
        }

        int rows = orderMapper.updateStatusById(id, orderStatusDTO.getStatus());
        if (rows < 1) {
            throw new BusinessException("Failed to update order status");
        }

        orders.setStatus(orderStatusDTO.getStatus());
        noticeService.syncOrderStatusNotice(orders);
    }

    private void validateCreateOrder(Long userId, OrderCreateDTO orderCreateDTO) {
        if (userId == null) {
            throw new BusinessException("User is not logged in");
        }
        if (orderCreateDTO == null) {
            throw new BusinessException("Order payload cannot be empty");
        }
        if (orderCreateDTO.getPetId() == null) {
            throw new BusinessException("Pet id cannot be empty");
        }
        if (orderCreateDTO.getContactName() == null || orderCreateDTO.getContactName().isBlank()) {
            throw new BusinessException("Contact name cannot be empty");
        }
        if (orderCreateDTO.getContactPhone() == null || orderCreateDTO.getContactPhone().isBlank()) {
            throw new BusinessException("Contact phone cannot be empty");
        }
        if (orderCreateDTO.getAddress() == null || orderCreateDTO.getAddress().isBlank()) {
            throw new BusinessException("Address cannot be empty");
        }
    }

    private boolean shouldRestoreStock(Integer oldStatus, Integer newStatus) {
        boolean toCanceledOrRefunded = newStatus != null && (newStatus == 3 || newStatus == 4);
        boolean alreadyCanceledOrRefunded = oldStatus != null && (oldStatus == 3 || oldStatus == 4);
        return toCanceledOrRefunded && !alreadyCanceledOrRefunded;
    }

    private void evictPetCache(Long petId) {
        if (petId != null) {
            redisTemplate.delete("pet:" + petId);
        }
    }

    private String generateOrderNo() {
        return "PETU" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }
}
