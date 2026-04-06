package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface OrderMapper {
    void insert(Orders orders);

    @Select("select * from orders where number = #{outTradeNo}")
    Orders getByNumber(String outTradeNo);

    void update(Orders orders);

    /**
     * 查询用户未支付的订单（防止重复提交）
     */
    Orders getUnpaidOrder(@Param("userId") Long userId, @Param("expireTime") LocalDateTime expireTime);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Update("update orders set status = 6 where id = #{id}")
    void cancel(Long id);

    Page<Orders> pageQuery4admin(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select count(id) from orders where status = #{status}")
    Integer countStatus(Integer toBeConfirmed);
}
