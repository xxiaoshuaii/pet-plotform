package com.pethub.mapper;

import com.pethub.pojo.dto.PetSaveDTO;
import com.pethub.pojo.entity.Pet;
import com.pethub.pojo.query.PetQuery;
import com.pethub.pojo.vo.PetVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PetMapper {

    List<PetVO> selectPage(PetQuery query);

    Pet selectEntityById(@Param("id") Long id);

    int insert(PetSaveDTO petSaveDTO);

    int updateById(@Param("id") Long id, @Param("pet") PetSaveDTO petSaveDTO);

    int deleteById(@Param("id") Long id);

    int updateStatusById(@Param("id") Long id, @Param("status") Integer status);
}
