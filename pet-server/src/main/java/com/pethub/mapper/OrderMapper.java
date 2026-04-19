package com.pethub.mapper;

import com.pethub.pojo.entity.Orders;
import com.pethub.pojo.query.OrderQuery;
import com.pethub.pojo.vo.OrderDetailVO;
import com.pethub.pojo.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    List<OrderVO> selectPage(OrderQuery query);

    OrderDetailVO selectDetailById(@Param("id") Long id);

    Orders selectEntityById(@Param("id") Long id);

    int insert(Orders orders);

    int updateStatusById(@Param("id") Long id, @Param("status") Integer status);

    int deleteById(@Param("id") Long id);

    @Select("select * from orders where create_time <= #{time} and status = #{status}")
    List<Orders> selectorder(LocalDateTime time, int status);
}
