package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

public interface DishService {
    
    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    DishVO getByIdWithFlavor(Long id);

    void save(DishDTO dishDTO);

    void drop(String[] ids);

    void alterDish(DishDTO dishDTO);

    void startOrStop(Integer status, Long id);
}
