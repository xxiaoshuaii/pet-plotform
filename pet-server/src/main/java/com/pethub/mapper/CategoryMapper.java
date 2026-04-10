package com.pethub.mapper;

import com.pethub.pojo.dto.CategorySaveDTO;
import com.pethub.pojo.entity.Category;
import com.pethub.pojo.query.CategoryQuery;
import com.pethub.pojo.vo.CategoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper {

    List<CategoryVO> selectPage(CategoryQuery query);

    Category selectEntityById(@Param("id") Long id);

    Category selectByName(@Param("name") String name);

    int insert(CategorySaveDTO categorySaveDTO);

    int updateById(@Param("id") Long id, @Param("category") CategorySaveDTO categorySaveDTO);

    int deleteById(@Param("id") Long id);

    int updateStatusById(@Param("id") Long id, @Param("status") Integer status);
}
