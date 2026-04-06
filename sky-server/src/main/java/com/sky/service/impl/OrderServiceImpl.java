package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;




    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //处理业务异常
        Long userId = BaseContext.getCurrentId();
// 查询当前用户 30秒内 的未支付订单
        LocalDateTime expireTime = LocalDateTime.now().minusSeconds(30);
        Orders existOrder = orderMapper.getUnpaidOrder(userId, expireTime);
        if (existOrder != null) {
            // 已有未支付订单，直接返回，不插入新数据
            return OrderSubmitVO.builder()
                    .id(existOrder.getId())
                    .orderNumber(existOrder.getNumber())
                    .orderAmount(existOrder.getAmount())
                    .orderTime(existOrder.getOrderTime())
                    .build();
        }

        AddressBook addressBookConditional = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBookConditional == null){
            //地址簿数据不存在
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //查询购物车数据
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.size() == 0){
            //购物车为空
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //插入订单数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBookConditional.getPhone());
        orders.setConsignee(addressBookConditional.getConsignee());
        orders.setUserId(userId);

        orderMapper.insert(orders);

        //插入订单明细数据
        List<OrderDetail> orderDetails = shoppingCartList.stream().map(item -> {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(item, orderDetail);
            orderDetail.setOrderId(orders.getId());
            return orderDetail;
        }).collect(Collectors.toList());

        orderDetailMapper.insertBatch(orderDetails);
        shoppingCartMapper.delete(shoppingCart);

        //返回订单数据
        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();

    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );

        JSONObject jsonObject = new JSONObject();
        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        shoppingCartMapper.drop(BaseContext.getCurrentId());

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    @Override
    public OrderVO getOrderDetail(Long id) {
        //查询订单
        Orders orders = orderMapper.getById(id);
        OrderVO orderVO = new OrderVO();

        AddressBook addressBook = addressBookMapper.getById(orders.getAddressBookId());
        String address = addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail();
        orders.setAddress(address);
        BeanUtils.copyProperties(orders, orderVO);

        log.info("订单详情：{}", orders.toString());


        //查询订单明细
        List<OrderDetail> orderDetailList = orderDetailMapper.list(id);
        orderDetailList.stream().map(item -> {
            if (item.getDishId() != null){
                Dish dish = dishMapper.getById(item.getDishId());
                item.setName(dish.getName());
            }else if (item.getSetmealId() != null){
                Setmeal setmeal = setmealMapper.getById(item.getSetmealId());
                item.setName(setmeal.getName());
            }
            return item;
        }).collect(Collectors.toList());

        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;


    }

    @Override
    public PageResult pageQuery4User(Integer page, Integer pageSize, Integer status) {
        //设置分页
        PageHelper.startPage(page, pageSize);

        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setStatus(status);
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());

        Page<Orders> pageInfo = orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> orderVOList = pageInfo.getResult().stream().map(item -> {
            List<OrderDetail> list = orderDetailMapper.list(item.getId());
            list.stream().map(detail -> {
                if (detail.getDishId() != null){
                    Dish dish = dishMapper.getById(detail.getDishId());
                    detail.setName(dish.getName());
                }else if (detail.getSetmealId() != null){
                    Setmeal setmeal = setmealMapper.getById(detail.getSetmealId());
                    detail.setName(setmeal.getName());
                }
                return detail;
            }).collect(Collectors.toList());
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(item, orderVO);
            orderVO.setOrderDetailList(list);
            return orderVO;
        }).collect(Collectors.toList());


        return new PageResult(pageInfo.getTotal(), orderVOList);
    }

    @Override
    public void again(Long id) {
        // 查询当前用户id
        Long userId = BaseContext.getCurrentId();

        // 根据订单id查询当前订单详情
        List<OrderDetail> orderDetailList = orderDetailMapper.list(id);

        // 将订单详情对象转换为购物车对象
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();

            // 将原订单详情里面的菜品信息重新复制到购物车对象中
            BeanUtils.copyProperties(x, shoppingCart, "id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            if (shoppingCart.getDishId() != null){
                shoppingCart.setName(dishMapper.getById(x.getDishId()).getName());
            }else {
                shoppingCart.setName(setmealMapper.getById(x.getSetmealId()).getName());
            }


            return shoppingCart;
        }).collect(Collectors.toList());

        // 将购物车对象批量添加到数据库
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    @Override
    public void cancel(Long id) {
        orderMapper.cancel(id);
    }

    @Override
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {


        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> pageInfo = orderMapper.pageQuery4admin(ordersPageQueryDTO);

        PageResult pageResult = new PageResult();
        pageResult.setTotal(pageInfo.getTotal());
        pageResult.setRecords(pageInfo.getResult());


        return pageResult;
    }

    @Override
    public OrderStatisticsVO getOrderStatistics() {
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);

        return orderStatisticsVO;
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();

        orderMapper.update(orders);
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(ordersRejectionDTO.getId());

        // 订单只有存在且状态为2（待接单）才可以拒单
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //支付状态
        Integer payStatus = ordersDB.getPayStatus();
        if (payStatus == Orders.PAID) {
            //用户已支付，需要退款
//            String refund = weChatPayUtil.refund(
//                    ordersDB.getNumber(),
//                    ordersDB.getNumber(),
//                    new BigDecimal(0.01),
//                    new BigDecimal(0.01));
//            log.info("申请退款：{}", refund);
        }
        // 拒单需要退款，根据订单id更新订单状态、拒单原因、取消时间
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(ordersCancelDTO.getId());

        //支付状态
        Integer payStatus = ordersDB.getPayStatus();
        if (payStatus == 1) {
            //用户已支付，需要退款
//            String refund = weChatPayUtil.refund(
//                    ordersDB.getNumber(),
//                    ordersDB.getNumber(),
//                    new BigDecimal(0.01),
//                    new BigDecimal(0.01));
//            log.info("申请退款：{}", refund);
        }

        // 管理端取消订单需要退款，根据订单id更新订单状态、取消原因、取消时间
        Orders orders = new Orders();
        orders.setId(ordersCancelDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);

    }

    @Override
    public void delivery(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在，并且状态为3
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为派送中
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);

        orderMapper.update(orders);
    }

    @Override
    public void complete(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在，并且状态为4
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态，状态转为完成
        orders.setStatus(Orders.COMPLETED);

        orderMapper.update(orders);
    }


}
