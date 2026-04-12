package com.pethub.service;

import com.pethub.pojo.dto.PostAuditDTO;
import com.pethub.pojo.dto.PostSaveDTO;
import com.pethub.pojo.query.PostQuery;
import com.pethub.pojo.vo.PageResultVO;
import com.pethub.pojo.vo.PostDetailVO;
import com.pethub.pojo.vo.PostVO;

public interface PostService {

    PageResultVO<PostVO> page(PostQuery query);

    PostDetailVO getById(Long id);

    PageResultVO<PostVO> pageByUserId(Long userId, PostQuery query);

    Long save(Long userId, PostSaveDTO postSaveDTO);

    void updateByUserId(Long userId, Long id, PostSaveDTO postSaveDTO);

    void updateStatus(Long id, PostAuditDTO postAuditDTO);

    boolean removeById(Long id);

    void removeByUserId(Long userId, Long id);
}
