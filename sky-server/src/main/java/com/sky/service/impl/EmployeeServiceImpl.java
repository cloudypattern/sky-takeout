package com.sky.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
<<<<<<< HEAD
import com.fasterxml.jackson.databind.ser.Serializers;
=======
>>>>>>> github/main
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
<<<<<<< HEAD
import com.sky.dto.PasswordEditDTO;
=======
>>>>>>> github/main
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

import java.util.List;

@Slf4j
@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
//    @DS("slave") 登录方法通常走主库，所以注释掉。默认走主库
    @Override
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        //进行md5加密，然后再进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        //拷贝
        BeanUtils.copyProperties(employeeDTO, employee);
        //设置默认状态
        employee.setStatus(StatusConstant.ENABLE);
        //设置默认密码
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        //设置创建时间、更新时间
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
        //设置创建人、更新人
//        employee.setCreateUser(BaseContext.getCurrentId());
//        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.insert(employee);
    }

    /**
     * 分页查询
     *
     * @param employeePageQueryDTO
     * @return
     */
//    @DS("slave") 从库 192.168.0.103 连不上，暂时注释掉，走主库
    @DS("slave")
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);
        long total = page.getTotal();
        List<Employee> records = page.getResult();
        return new PageResult(total, records);
    }

    /**
     * 启用禁用员工账号
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();
        employeeMapper.update(employee);
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @DS("slave")
    @Override
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        return employee;
    }

    @Override
    public void update(EmployeeDTO employeeDTO) {

        Employee employee = new Employee();

        BeanUtils.copyProperties(employeeDTO, employee);

//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.update(employee);
    }

<<<<<<< HEAD
    /**
     * 修改员工密码
     * @param passwordEditDTO
     */
    @Override
    public void editPassword(PasswordEditDTO passwordEditDTO) {
        Employee employee = employeeMapper.getById(BaseContext.getCurrentId());
//        Employee employee = employeeMapper.getById(passwordEditDTO.getEmpId());
        String originalPassword = employee.getPassword();
        String oldPassword = DigestUtils.md5DigestAsHex(passwordEditDTO.getOldPassword().getBytes());
        if (!oldPassword.equals(originalPassword)) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }else {
            String newPassword = passwordEditDTO.getNewPassword();
            String password = DigestUtils.md5DigestAsHex(newPassword.getBytes());
            employee.setPassword(password);
            employeeMapper.update(employee);
        }
    }

=======
>>>>>>> github/main
}
