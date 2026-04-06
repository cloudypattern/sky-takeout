package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
public class DIshController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 菜品管理分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        PageResult page = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(page);
    }

    /**
     * 根据id查询菜品和对应的口味数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id){
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * 新增菜品
     * @param
     * @return
     */
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO){

        dishService.save(dishDTO);

        deleteCache("dish_" + dishDTO.getCategoryId());

        return Result.success();
    }

    /**
     * 删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result drop(String[] ids){
        dishService.drop(ids);
        deleteCache("dish_*");
        return Result.success();
    }

    /**
     * 修改菜品
     * @param dishDTO
     * @return
     */
    @PutMapping
    public Result alterDish(@RequestBody DishDTO dishDTO){
        dishService.alterDish(dishDTO);
        deleteCache("dish_*");
        return Result.success();
    }

    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status, Long id){
        dishService.startOrStop(status,id);
        deleteCache("dish_*");
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<DishVO>> list(Long categoryId){

        List<DishVO> dishes = dishService.list(categoryId);

        return Result.success(dishes);
    }

    private void deleteCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }

}
