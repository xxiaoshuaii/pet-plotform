package com.pethub.service;

import com.pethub.pojo.dto.PostAuditDTO;
import com.pethub.pojo.query.PostQuery;
import com.pethub.pojo.vo.PageResultVO;
import com.pethub.pojo.vo.PostDetailVO;
import com.pethub.pojo.vo.PostVO;

public interface PostService {

    PageResultVO<PostVO> page(PostQuery query);

    PostDetailVO getById(Long id);

    void updateStatus(Long id, PostAuditDTO postAuditDTO);

    boolean removeById(Long id);
}
