package com.pethub.mapper;

import com.pethub.pojo.entity.AiChatSession;
import com.pethub.pojo.vo.AiChatSessionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AiChatSessionMapper {

    int insert(AiChatSession session);

    AiChatSession selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    List<AiChatSessionVO> selectListByUserId(@Param("userId") Long userId);

    int touchSession(@Param("id") Long id,
                     @Param("title") String title,
                     @Param("lastMessageTime") LocalDateTime lastMessageTime,
                     @Param("updateTime") LocalDateTime updateTime);

    int touchSessionActivity(@Param("id") Long id,
                             @Param("lastMessageTime") LocalDateTime lastMessageTime,
                             @Param("updateTime") LocalDateTime updateTime);

    int softDeleteByIdAndUserId(@Param("id") Long id,
                                @Param("userId") Long userId,
                                @Param("updateTime") LocalDateTime updateTime);
}
