package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {

        shoppingCartService.add(shoppingCartDTO);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<ShoppingCart>> list() {
        Long userId = BaseContext.getCurrentId();
        List<ShoppingCart> list = shoppingCartService.list(userId);
        return Result.success(list);
    }

    @PostMapping("/sub")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        shoppingCartService.sub(shoppingCartDTO);
        return Result.success();
    }

    @DeleteMapping("/clean")
    public Result clean() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartService.clean(userId);
        return Result.success();
    }

}
