package com.pethub.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pethub.common.exception.BusinessException;
import com.pethub.mapper.PetMapper;
import com.pethub.pojo.dto.PetSaveDTO;
import com.pethub.pojo.dto.PetStatusDTO;
import com.pethub.pojo.entity.Pet;
import com.pethub.pojo.query.PetQuery;
import com.pethub.pojo.vo.PageResultVO;
import com.pethub.pojo.vo.PetVO;
import com.pethub.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PetServiceImpl implements PetService {

    private final PetMapper petMapper;

    @Override
    public PageResultVO<PetVO> page(PetQuery query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();

        PageHelper.startPage(pageNum, pageSize);
        List<PetVO> records = petMapper.selectPage(query);
        PageInfo<PetVO> pageInfo = new PageInfo<>(records);

        return new PageResultVO<>(
                pageInfo.getList(),
                pageInfo.getTotal(),
                pageInfo.getPageNum(),
                pageInfo.getPageSize()
        );
    }

    @Override
    public void save(PetSaveDTO petSaveDTO) {
        validatePetSaveDTO(petSaveDTO);
        int rows = petMapper.insert(petSaveDTO);
        if (rows < 1) {
            throw new BusinessException("新增宠物失败");
        }
    }

    @Override
    public void update(Long id, PetSaveDTO petSaveDTO) {
        validatePetSaveDTO(petSaveDTO);
        Pet pet = petMapper.selectEntityById(id);
        if (pet == null) {
            throw new BusinessException("宠物不存在");
        }

        int rows = petMapper.updateById(id, petSaveDTO);
        if (rows < 1) {
            throw new BusinessException("更新宠物失败");
        }
    }

    @Override
    public boolean removeById(Long id) {
        Pet pet = petMapper.selectEntityById(id);
        if (pet == null) {
            throw new BusinessException("宠物不存在");
        }
        return petMapper.deleteById(id) > 0;
    }

    @Override
    public void updateStatus(Long id, PetStatusDTO petStatusDTO) {
        if (petStatusDTO.getStatus() == null) {
            throw new BusinessException("宠物状态不能为空");
        }

        Pet pet = petMapper.selectEntityById(id);
        if (pet == null) {
            throw new BusinessException("宠物不存在");
        }

        int rows = petMapper.updateStatusById(id, petStatusDTO.getStatus());
        if (rows < 1) {
            throw new BusinessException("更新宠物状态失败");
        }
    }

    private void validatePetSaveDTO(PetSaveDTO petSaveDTO) {
        if (petSaveDTO.getName() == null || petSaveDTO.getName().isBlank()) {
            throw new BusinessException("宠物名称不能为空");
        }
        if (petSaveDTO.getCategoryId() == null) {
            throw new BusinessException("宠物分类不能为空");
        }
        if (petSaveDTO.getBreed() == null || petSaveDTO.getBreed().isBlank()) {
            throw new BusinessException("宠物品种不能为空");
        }
        if (petSaveDTO.getAge() == null) {
            throw new BusinessException("宠物年龄不能为空");
        }
        if (petSaveDTO.getPrice() == null) {
            throw new BusinessException("宠物价格不能为空");
        }
        if (petSaveDTO.getStock() == null) {
            throw new BusinessException("宠物库存不能为空");
        }
        if (petSaveDTO.getCoverUrl() == null || petSaveDTO.getCoverUrl().isBlank()) {
            throw new BusinessException("宠物封面图不能为空");
        }
        if (petSaveDTO.getStatus() == null) {
            throw new BusinessException("宠物状态不能为空");
        }
    }
}
