package com.pethub.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pethub.common.exception.BusinessException;
import com.pethub.mapper.UserMapper;
import com.pethub.pojo.dto.UserProfileUpdateDTO;
import com.pethub.pojo.dto.UserStatusDTO;
import com.pethub.pojo.query.UserQuery;
import com.pethub.pojo.vo.PageResultVO;
import com.pethub.pojo.vo.UserDetailVO;
import com.pethub.pojo.vo.UserVO;
import com.pethub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public PageResultVO<UserVO> page(UserQuery query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();

        PageHelper.startPage(pageNum, pageSize);
        List<UserVO> records = userMapper.selectPage(query);
        PageInfo<UserVO> pageInfo = new PageInfo<>(records);

        return new PageResultVO<>(pageInfo.getList(), pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize());
    }

    @Override
    public UserDetailVO getById(Long id) {
        UserDetailVO userDetailVO = userMapper.selectById(id);
        if (userDetailVO == null) {
            throw new BusinessException("用户不存在");
        }
        return userDetailVO;
    }

    @Override
    public void updateProfile(Long id, UserProfileUpdateDTO userProfileUpdateDTO) {
        if (id == null) {
            throw new BusinessException("未登录或登录已失效");
        }
        if (userProfileUpdateDTO == null) {
            throw new BusinessException("个人资料参数不能为空");
        }

        UserDetailVO current = userMapper.selectById(id);
        if (current == null) {
            throw new BusinessException("用户不存在");
        }

        int rows = userMapper.updateProfileById(id, userProfileUpdateDTO);
        if (rows < 1) {
            throw new BusinessException("更新个人资料失败");
        }
    }

    @Override
    public void updateStatus(Long id, UserStatusDTO userStatusDTO) {
        if (userStatusDTO.getStatus() == null) {
            throw new BusinessException("用户状态不能为空");
        }

        UserDetailVO current = userMapper.selectById(id);
        if (current == null) {
            throw new BusinessException("用户不存在");
        }

        int rows = userMapper.updateStatusById(id, userStatusDTO.getStatus());
        if (rows < 1) {
            throw new BusinessException("更新用户状态失败");
        }
    }
}
