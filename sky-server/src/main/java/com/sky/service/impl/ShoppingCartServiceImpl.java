package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private DishMapper dishMapper;

    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);

        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        if (list != null && !list.isEmpty()) {
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);

            shoppingCartMapper.update(cart);
        }else {

            ShoppingCart newCart = null;

            if (shoppingCart.getSetmealId() != null){
                Setmeal setmeal = setmealMapper.getById(shoppingCart.getSetmealId());
                 newCart = ShoppingCart.builder()
                         .userId(shoppingCart.getUserId()) // 必须加！
                         .setmealId(shoppingCart.getSetmealId()) // 必须加！
                         .name(setmeal.getName())
                        .number(1)
                        .amount(setmeal.getPrice())
                        .image(setmeal.getImage())
                        .createTime(LocalDateTime.now()).build();

            }else {

                // 👇 下面是我帮你补齐的逻辑，和上面完全对齐
                Dish dish = dishMapper.getById(shoppingCart.getDishId());
                newCart = ShoppingCart.builder()
                        .userId(shoppingCart.getUserId()) // 必须加！
                        .dishId(shoppingCart.getDishId()) // 正确
                        .name(dish.getName())
                        .dishFlavor(shoppingCart.getDishFlavor())
                        .number(1)
                        .amount(dish.getPrice())
                        .image(dish.getImage())
                        .createTime(LocalDateTime.now())
                        .build();

            }
            shoppingCartMapper.insert(newCart);
        }






    }

    @Override
    public List<ShoppingCart> list(Long userId) {
        ShoppingCart shoppingCart = ShoppingCart.builder()
                        .userId(userId).build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        return list;
    }

    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        ShoppingCart cart = list.get(0);
        if (cart.getNumber() == 1){
            shoppingCartMapper.delete(cart);
        }
        cart.setNumber(cart.getNumber() - 1);
        shoppingCartMapper.sub(cart);
    }

    @Override
    public void clean(Long userId) {
        shoppingCartMapper.drop(userId);
    }
}
