package com.sky.controller.user;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){

        OrderSubmitVO orderSubmitVO = orderService.submit(ordersSubmitDTO);

        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        orderService.paySuccess(ordersPaymentDTO.getOrderNumber());
        return Result.success(orderPaymentVO);
    }

    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id){
        OrderVO orderDetail = orderService.getOrderDetail(id);
        return Result.success(orderDetail);
    }

    @GetMapping("/historyOrders")
    public Result<PageResult> page(Integer page, Integer pageSize, Integer status){
        PageResult pageResult = orderService.pageQuery4User(page, pageSize, status);
        return Result.success(pageResult);
    }

    @PostMapping("/repetition/{id}")
    public Result again (@PathVariable Long id){
        orderService.again(id);
        return Result.success();
    }

    @PutMapping("/cancel/{id}")
    public Result cancel(@PathVariable Long id){
        orderService.cancel(id);
        return Result.success();
    }

}
