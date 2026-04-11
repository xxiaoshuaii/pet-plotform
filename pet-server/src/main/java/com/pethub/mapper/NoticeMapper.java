package com.pethub.mapper;

import com.pethub.pojo.entity.Notice;
import com.pethub.pojo.vo.NoticeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoticeMapper {

    List<NoticeVO> selectList();

    Notice selectEntityById(@Param("id") Long id);

    Long countUnread();

    int readById(@Param("id") Long id);

    int readAll();

    int softDeleteByOrderId(@Param("orderId") Long orderId);

    int insert(Notice notice);
}
