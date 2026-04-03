package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SetmealDishMapper {


    void insertBatch(@Param("setmealDishes") List<SetmealDish> setmealDishes);

    void deleteBySetmealId(Long id);

    List<SetmealDish> getBySetmealId(Long id);
}
