package com.pethub.mapper;

import com.pethub.pojo.dto.AddressSaveDTO;
import com.pethub.pojo.entity.UserAddress;
import com.pethub.pojo.vo.AddressVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserAddressMapper {

    List<AddressVO> selectListByUserId(@Param("userId") Long userId);

    AddressVO selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    UserAddress selectEntityByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    AddressVO selectDefaultByUserId(@Param("userId") Long userId);

    int insert(@Param("userId") Long userId, @Param("address") AddressSaveDTO addressSaveDTO);

    int updateById(@Param("id") Long id, @Param("userId") Long userId, @Param("address") AddressSaveDTO addressSaveDTO);

    int clearDefaultByUserId(@Param("userId") Long userId);

    int setDefaultById(@Param("id") Long id, @Param("userId") Long userId);

    int deleteById(@Param("id") Long id, @Param("userId") Long userId);
}
