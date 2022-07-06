package com.fisk.system.client;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.system.dto.userinfo.UserDTO;
import com.fisk.system.dto.userinfo.UserDropDTO;
import com.fisk.system.dto.userinfo.UserGroupQueryDTO;
import com.fisk.system.dto.userinfo.UserPowerDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Lock
 *
 * 对外开放的是controller层里的方法,注意:
 *  1.没有方法体
 *  2.不再是ResponseEntity<param>接受参数,而是ResponseEntity<param>中的param
 *  3.方法上代表CRUD的注解不变,并且要全路径,包括controller类上的路径
 */
@FeignClient("user-service")
public interface UserClient {

    /**
     * 根据用户名和密码查询用户
     *
     * @param userAccount 用户名
     * @param password 密码
     * @return 前端json格式传来的,@RequestParam接对象
     */
    @GetMapping("/info")
    ResultEntity<UserDTO> queryUser(
            @RequestParam("userAccount") String userAccount,
            @RequestParam("password") String password);

    /**
     * 获取数据服务中文名称列表
     * @return
     */
    @GetMapping("/attribute/getServiceRegistryList")
    ResultEntity<Object> getServiceRegistryList();

    /**
     * 获取系统用户集合
     * @param ids
     * @return
     */
    @PostMapping("/info/getUserListByIds")
    ResultEntity<List<UserDTO>> getUserListByIds(@RequestBody List<Long> ids);

    /**
     * 用户组筛选系统用户
     * @param dto
     * @return
     */
    @PostMapping("/auth/userGroupQuery")
    ResultEntity<Page<UserPowerDTO>> userGroupQuery(@RequestBody UserGroupQueryDTO dto);

    /**
     * 获取用户下拉数据
     * @return
     */
    @GetMapping("/info/listUserDrops")
    ResultEntity<List<UserDropDTO>> listUserDrops();

    /**
     * 查询FiData所有数据源信息
     * @return
     */
    @PostMapping("/datasource/getAll")
    ResultEntity<List<DataSourceDTO>> getAllFiDataDataSource();

    /**
     * 查询FiData指定数据源信息
     * @return
     */
    @GetMapping("/datasource/getById/{datasourceId}")
    ResultEntity<DataSourceDTO> getFiDataDataSourceById(@RequestParam("datasourceId") int datasourceId);
}
