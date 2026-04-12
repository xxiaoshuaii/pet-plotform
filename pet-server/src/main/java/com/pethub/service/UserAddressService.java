package com.pethub.service;

import com.pethub.pojo.dto.AddressSaveDTO;
import com.pethub.pojo.vo.AddressVO;

import java.util.List;

public interface UserAddressService {

    List<AddressVO> listByCurrentUser(Long userId);

    AddressVO getById(Long userId, Long id);

    AddressVO getDefaultAddress(Long userId);

    Long save(Long userId, AddressSaveDTO addressSaveDTO);

    void update(Long userId, Long id, AddressSaveDTO addressSaveDTO);

    void setDefault(Long userId, Long id);

    void removeById(Long userId, Long id);
}
