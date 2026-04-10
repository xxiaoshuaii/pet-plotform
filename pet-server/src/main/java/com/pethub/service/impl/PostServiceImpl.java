package com.pethub.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pethub.common.exception.BusinessException;
import com.pethub.mapper.PostMapper;
import com.pethub.pojo.dto.PostAuditDTO;
import com.pethub.pojo.entity.Post;
import com.pethub.pojo.query.PostQuery;
import com.pethub.pojo.vo.PageResultVO;
import com.pethub.pojo.vo.PostDetailVO;
import com.pethub.pojo.vo.PostVO;
import com.pethub.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;

    @Override
    public PageResultVO<PostVO> page(PostQuery query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();

        PageHelper.startPage(pageNum, pageSize);
        List<PostVO> records = postMapper.selectPage(query);
        PageInfo<PostVO> pageInfo = new PageInfo<>(records);

        return new PageResultVO<>(
                pageInfo.getList(),
                pageInfo.getTotal(),
                pageInfo.getPageNum(),
                pageInfo.getPageSize()
        );
    }

    @Override
    public PostDetailVO getById(Long id) {
        PostDetailVO postDetailVO = postMapper.selectDetailById(id);
        if (postDetailVO == null) {
            throw new BusinessException("帖子不存在");
        }
        return postDetailVO;
    }

    @Override
    public void updateStatus(Long id, PostAuditDTO postAuditDTO) {
        if (postAuditDTO.getStatus() == null) {
            throw new BusinessException("审核状态不能为空");
        }
        if (postAuditDTO.getStatus() != 1 && postAuditDTO.getStatus() != 2) {
            throw new BusinessException("审核状态只能是通过或驳回");
        }

        Post post = postMapper.selectEntityById(id);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }

        String rejectReason = postAuditDTO.getStatus() == 2 ? postAuditDTO.getRejectReason() : null;
        int rows = postMapper.updateStatusById(id, postAuditDTO.getStatus(), rejectReason);
        if (rows < 1) {
            throw new BusinessException("更新帖子审核状态失败");
        }
    }

    @Override
    public boolean removeById(Long id) {
        Post post = postMapper.selectEntityById(id);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        return postMapper.deleteById(id) > 0;
    }
}
