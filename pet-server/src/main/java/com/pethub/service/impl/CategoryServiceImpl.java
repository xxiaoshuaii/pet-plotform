package com.pethub.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pethub.common.exception.BusinessException;
import com.pethub.mapper.CategoryMapper;
import com.pethub.pojo.dto.CategorySaveDTO;
import com.pethub.pojo.dto.CategoryStatusDTO;
import com.pethub.pojo.entity.Category;
import com.pethub.pojo.query.CategoryQuery;
import com.pethub.pojo.vo.CategoryVO;
import com.pethub.pojo.vo.PageResultVO;
import com.pethub.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public PageResultVO<CategoryVO> page(CategoryQuery query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 5 : query.getPageSize();

        PageHelper.startPage(pageNum, pageSize);
        List<CategoryVO> records = categoryMapper.selectPage(query);
        PageInfo<CategoryVO> pageInfo = new PageInfo<>(records);

        return new PageResultVO<>(
                pageInfo.getList(),
                pageInfo.getTotal(),
                pageInfo.getPageNum(),
                pageInfo.getPageSize()
        );
    }

    @Override
    public void save(CategorySaveDTO categorySaveDTO) {
        validateCategory(categorySaveDTO);

        Category category = categoryMapper.selectByName(categorySaveDTO.getName());
        if (category != null && category.getIsDeleted() != null && category.getIsDeleted() == 0) {
            throw new BusinessException("分类名称已存在");
        }

        int rows = categoryMapper.insert(categorySaveDTO);
        if (rows < 1) {
            throw new BusinessException("新增分类失败");
        }
    }

    @Override
    public void update(Long id, CategorySaveDTO categorySaveDTO) {
        validateCategory(categorySaveDTO);

        Category current = categoryMapper.selectEntityById(id);
        if (current == null) {
            throw new BusinessException("分类不存在");
        }

        Category sameNameCategory = categoryMapper.selectByName(categorySaveDTO.getName());
        if (sameNameCategory != null && !sameNameCategory.getId().equals(id)
                && sameNameCategory.getIsDeleted() != null && sameNameCategory.getIsDeleted() == 0) {
            throw new BusinessException("分类名称已存在");
        }

        int rows = categoryMapper.updateById(id, categorySaveDTO);
        if (rows < 1) {
            throw new BusinessException("更新分类失败");
        }
    }

    @Override
    public boolean removeById(Long id) {
        Category current = categoryMapper.selectEntityById(id);
        if (current == null) {
            throw new BusinessException("分类不存在");
        }
        return categoryMapper.deleteById(id) > 0;
    }

    @Override
    public void updateStatus(Long id, CategoryStatusDTO categoryStatusDTO) {
        if (categoryStatusDTO.getStatus() == null) {
            throw new BusinessException("分类状态不能为空");
        }

        Category current = categoryMapper.selectEntityById(id);
        if (current == null) {
            throw new BusinessException("分类不存在");
        }

        int rows = categoryMapper.updateStatusById(id, categoryStatusDTO.getStatus());
        if (rows < 1) {
            throw new BusinessException("更新分类状态失败");
        }
    }

    private void validateCategory(CategorySaveDTO categorySaveDTO) {
        if (categorySaveDTO.getName() == null || categorySaveDTO.getName().isBlank()) {
            throw new BusinessException("分类名称不能为空");
        }
        if (categorySaveDTO.getStatus() == null) {
            throw new BusinessException("分类状态不能为空");
        }
    }
}
