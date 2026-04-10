package com.pethub.service;

import com.pethub.pojo.dto.CategorySaveDTO;
import com.pethub.pojo.dto.CategoryStatusDTO;
import com.pethub.pojo.query.CategoryQuery;
import com.pethub.pojo.vo.CategoryVO;
import com.pethub.pojo.vo.PageResultVO;

public interface CategoryService {

    PageResultVO<CategoryVO> page(CategoryQuery query);

    void save(CategorySaveDTO categorySaveDTO);

    void update(Long id, CategorySaveDTO categorySaveDTO);

    boolean removeById(Long id);

    void updateStatus(Long id, CategoryStatusDTO categoryStatusDTO);
}
