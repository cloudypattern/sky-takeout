package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

public interface SetmealService {
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    void save(SetmealDTO setmealDTO);

    void update(SetmealDTO setmealDTO);

    SetmealVO getById(Long id);

    void startOrStop(Integer status, Long id);

    void drop(String[] ids);
}
