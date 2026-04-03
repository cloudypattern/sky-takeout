package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
<<<<<<< HEAD
import com.sky.dto.EmployeeDTO;
=======
>>>>>>> github/main
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
<<<<<<< HEAD
import org.apache.ibatis.annotations.Update;
=======
>>>>>>> github/main

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 插入员工数据
     * @param employee
     */
    @AutoFill(value = OperationType.INSERT)
    void insert(Employee employee);

    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    @AutoFill(value = OperationType.UPDATE)
    void update(Employee employee);

<<<<<<< HEAD
    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @Select("select * from employee where id = #{id}")
    Employee getById(Long id);

=======
    @Select("select * from employee where id = #{id}")
    Employee getById(Long id);
>>>>>>> github/main
}
