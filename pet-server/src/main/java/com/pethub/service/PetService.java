package com.pethub.service;

import com.pethub.pojo.dto.PetSaveDTO;
import com.pethub.pojo.dto.PetStatusDTO;
import com.pethub.pojo.query.PetQuery;
import com.pethub.pojo.vo.PageResultVO;
import com.pethub.pojo.vo.PetVO;

public interface PetService {

    PageResultVO<PetVO> page(PetQuery query);

    PetVO getById(Long id);

    void save(PetSaveDTO petSaveDTO);

    void update(Long id, PetSaveDTO petSaveDTO);

    boolean removeById(Long id);

    void updateStatus(Long id, PetStatusDTO petStatusDTO);
}
