package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

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
        //对前端传递过来的明文密码进行md5加密
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

    /**
     * 新增员工
     * @param employeeDTO
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        // 构建dao对象
        Employee employee = new Employee();

        // 拷贝DTO对象给dao
        BeanUtils.copyProperties(employeeDTO,employee);

        // 设置新建用户默认状态
        employee.setStatus(StatusConstant.ENABLE);

        // 设置新建用户默认密码为123456
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        // 设置创建时间和修改时间
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());


        // 设置当前记录创建人的ID和修改人的ID
//        employee.setCreateUser(BaseContext.getCurrentId());
//        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.insert(employee);
    }

    /**
     * 员工分页查询
     * @param employeePageQueryDTO
     * @return
     */
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO){
        // select * from employee limit(0,10) 使用 mybits 提供的PageHelper进行拼接查询。
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);
        long total = page.getTotal();
        List<Employee> records = page.getResult();
        return new PageResult(total,records);
    }

    /**
     * 启用禁用员工账号
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id){
        // update employee set status = ? where id = ?

        // 经典创建对象和赋值
        //Employee employee = new Employee();
        //employee.setStatus(status);

        // @builder 注解构建
        Employee employee= Employee.builder().status(status).id(id).build();
        employeeMapper.update(employee);
    }

    /**
     * 根据Id查询员工信息
     * @param id
     * @return
     */
    public Employee getById(Long id){
        // select * from employee where id = ?

        // 创建对象并进行查询
        Employee employee = employeeMapper.getById(id);
        employee.setPassword("****");

        return employee;
    }

    /**
     * 更新员工信息
     * @param employeeDTO
     */
    public void update(EmployeeDTO employeeDTO){
        // update employee set (keys...) val(...) where id = ?

        // 创建dao对象，并把dto属性拷贝给dao
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);

        // 记录当前修改时间
//        employee.setUpdateTime(LocalDateTime.now());
        // 记录当前修改用户信息的执行人
//        employee.setUpdateUser(BaseContext.getCurrentId());
        // 使用Mybits进行update操作。
        employeeMapper.update(employee);
    }
}
