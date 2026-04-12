package com.pethub.service.impl;

import com.pethub.common.exception.BusinessException;
import com.pethub.mapper.UserAddressMapper;
import com.pethub.pojo.dto.AddressSaveDTO;
import com.pethub.pojo.entity.UserAddress;
import com.pethub.pojo.vo.AddressVO;
import com.pethub.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAddressServiceImpl implements UserAddressService {

    private final UserAddressMapper userAddressMapper;

    @Override
    public List<AddressVO> listByCurrentUser(Long userId) {
        validateUserId(userId);
        return userAddressMapper.selectListByUserId(userId);
    }

    @Override
    public AddressVO getById(Long userId, Long id) {
        validateUserId(userId);
        AddressVO addressVO = userAddressMapper.selectByIdAndUserId(id, userId);
        if (addressVO == null) {
            throw new BusinessException("收货地址不存在");
        }
        return addressVO;
    }

    @Override
    public AddressVO getDefaultAddress(Long userId) {
        validateUserId(userId);
        return userAddressMapper.selectDefaultByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(Long userId, AddressSaveDTO addressSaveDTO) {
        validateUserId(userId);
        validateAddress(addressSaveDTO);

        normalizeDefaultFlag(addressSaveDTO);
        if (addressSaveDTO.getIsDefault() == 1 || userAddressMapper.selectDefaultByUserId(userId) == null) {
          userAddressMapper.clearDefaultByUserId(userId);
          addressSaveDTO.setIsDefault(1);
        }

        int rows = userAddressMapper.insert(userId, addressSaveDTO);
        if (rows < 1) {
            throw new BusinessException("新增收货地址失败");
        }
        return addressSaveDTO.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long userId, Long id, AddressSaveDTO addressSaveDTO) {
        validateUserId(userId);
        validateAddress(addressSaveDTO);

        UserAddress current = userAddressMapper.selectEntityByIdAndUserId(id, userId);
        if (current == null) {
            throw new BusinessException("收货地址不存在");
        }

        normalizeDefaultFlag(addressSaveDTO);
        if (addressSaveDTO.getIsDefault() == 1) {
            userAddressMapper.clearDefaultByUserId(userId);
        } else if (current.getIsDefault() != null && current.getIsDefault() == 1) {
            addressSaveDTO.setIsDefault(1);
        }

        int rows = userAddressMapper.updateById(id, userId, addressSaveDTO);
        if (rows < 1) {
            throw new BusinessException("更新收货地址失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long userId, Long id) {
        validateUserId(userId);

        UserAddress current = userAddressMapper.selectEntityByIdAndUserId(id, userId);
        if (current == null) {
            throw new BusinessException("收货地址不存在");
        }

        userAddressMapper.clearDefaultByUserId(userId);
        int rows = userAddressMapper.setDefaultById(id, userId);
        if (rows < 1) {
            throw new BusinessException("设置默认地址失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeById(Long userId, Long id) {
        validateUserId(userId);

        UserAddress current = userAddressMapper.selectEntityByIdAndUserId(id, userId);
        if (current == null) {
            throw new BusinessException("收货地址不存在");
        }

        int rows = userAddressMapper.deleteById(id, userId);
        if (rows < 1) {
            throw new BusinessException("删除收货地址失败");
        }

        if (current.getIsDefault() != null && current.getIsDefault() == 1) {
            List<AddressVO> addressList = userAddressMapper.selectListByUserId(userId);
            if (!addressList.isEmpty()) {
                userAddressMapper.setDefaultById(addressList.get(0).getId(), userId);
            }
        }
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException("未登录或登录已失效");
        }
    }

    private void validateAddress(AddressSaveDTO addressSaveDTO) {
        if (addressSaveDTO == null) {
            throw new BusinessException("地址参数不能为空");
        }
        if (addressSaveDTO.getContactName() == null || addressSaveDTO.getContactName().isBlank()) {
            throw new BusinessException("联系人不能为空");
        }
        if (addressSaveDTO.getContactPhone() == null || addressSaveDTO.getContactPhone().isBlank()) {
            throw new BusinessException("联系电话不能为空");
        }
        if (addressSaveDTO.getProvince() == null || addressSaveDTO.getProvince().isBlank()) {
            throw new BusinessException("省份不能为空");
        }
        if (addressSaveDTO.getCity() == null || addressSaveDTO.getCity().isBlank()) {
            throw new BusinessException("城市不能为空");
        }
        if (addressSaveDTO.getDistrict() == null || addressSaveDTO.getDistrict().isBlank()) {
            throw new BusinessException("区县不能为空");
        }
        if (addressSaveDTO.getDetailAddress() == null || addressSaveDTO.getDetailAddress().isBlank()) {
            throw new BusinessException("详细地址不能为空");
        }
    }

    private void normalizeDefaultFlag(AddressSaveDTO addressSaveDTO) {
        if (addressSaveDTO.getIsDefault() == null || addressSaveDTO.getIsDefault() != 1) {
            addressSaveDTO.setIsDefault(0);
        }
    }
}
