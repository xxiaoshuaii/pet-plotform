package com.pethub.mapper;

import com.pethub.pojo.entity.AiChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiChatMessageMapper {

    int insert(AiChatMessage message);

    List<AiChatMessage> selectListBySessionId(@Param("sessionId") Long sessionId);

    List<AiChatMessage> selectRecentBySessionId(@Param("sessionId") Long sessionId, @Param("limit") Integer limit);

    int softDeleteBySessionId(@Param("sessionId") Long sessionId, @Param("updateTime") java.time.LocalDateTime updateTime);
}
