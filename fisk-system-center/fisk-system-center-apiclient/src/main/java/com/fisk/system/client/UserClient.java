package com.fisk.system.client;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.service.sqlparser.model.TableMetaDataObject;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.system.dto.datasource.DataSourceResultDTO;
import com.fisk.system.dto.datasource.DataSourceSaveDTO;
import com.fisk.system.dto.roleinfo.RoleInfoDTO;
import com.fisk.system.dto.userinfo.UserDTO;
import com.fisk.system.dto.userinfo.UserDropDTO;
import com.fisk.system.dto.userinfo.UserGroupQueryDTO;
import com.fisk.system.dto.userinfo.UserPowerDTO;
import com.fisk.system.vo.emailserver.EmailServerVO;
import com.fisk.system.vo.roleinfo.RoleInfoVo;
import io.swagger.annotations.ApiOperation;
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
     * 查询所有数据源信息
     * @return
     */
    @PostMapping("/datasource/getAll")
    ResultEntity<List<DataSourceDTO>> getAll();

    /**
     * 查询FiData数据源信息
     * @return
     */
    @PostMapping("/datasource/getAllFiDataDataSource")
    ResultEntity<List<DataSourceDTO>> getAllFiDataDataSource();

    /**
     * 查询外部数据源信息
     * @return
     */
    @PostMapping("/datasource/getAllExternalDataSource")
    ResultEntity<List<DataSourceDTO>> getAllExternalDataSource();

    /**
     * 查询FiData指定数据源信息
     * @return
     */
    @GetMapping("/datasource/getById/{datasourceId}")
    ResultEntity<DataSourceDTO> getFiDataDataSourceById(@RequestParam("datasourceId") int datasourceId);

    /**
     * 查询所有邮件服务器信息
     * @return
     */
    @PostMapping("/emailserver/getEmailServerList")
    ResultEntity<List<EmailServerVO>> getEmailServerList();

    /**
     * 根据ID查询邮件服务器信息
     * @return
     */
    @GetMapping("/emailserver/getById/{id}")
    ResultEntity<EmailServerVO> getEmailServerById(@RequestParam("id") int id);

    /**
     * 根据ids获取角色列表详情
     * @return
     */
    @PostMapping("/role/getRoles")
    ResultEntity<List<RoleInfoDTO>> getRoles(@RequestBody List<Integer> ids);

    /**
     * 根据用户id获取用户角色信息
     * @return
     */
    @GetMapping("/role/getRolebyUserId/{userId}")
    ResultEntity<List<RoleInfoDTO>> getRolebyUserId(@RequestParam("userId") int userId);

    /**
     * 根据用户姓名模糊查询用户id
     * @return
     */
    @GetMapping("/info/getUserIdByUserName/{userName}")
    ResultEntity<List<Integer>> getUserIdByUserName(@RequestParam("userName") String userName);

    /**
     * 获取所有角色及角色下用户列表
     * @return
     */
    @GetMapping("/role/getTreeRols")
    ResultEntity<List<RoleInfoVo>> getTreeRols();
    /**
     * 菜单列表
     * @return
     */
    @GetMapping("/ServiceRegistry/getList")
    ResultEntity<Object> getMenuList();

    /**
     * 获取用户列表
     * @return
     */
    @GetMapping("/info/getAllUserList")
    ResultEntity<List<UserDTO>> getAllUserList();

    /**
     * 获取菜单列表
     *
     * @return
     */
    @GetMapping("/auth/getAllMenuList")
    ResultEntity<Object> getAllMenuList();

    /**
     * 同步数据接入数据源
     *
     * @param dto
     * @return
     */
    @PostMapping("/datasource/insertDataSourceByAccess")
    @ApiOperation("同步数据接入数据源")
    ResultEntity<DataSourceResultDTO> insertDataSourceByAccess(@RequestBody DataSourceSaveDTO dto);

    /**
     * 添加系统数据源，设置nifi启动参数
     * @param dto
     * @return
     */
    @PostMapping("/datasource/add")
    ResultEntity<Object> addData(@RequestBody DataSourceSaveDTO dto);

    /**
     * 修改系统数据源，设置nifi启动参数
     * @param dto
     * @return
     */
    @PutMapping("/datasource/edit")
    ResultEntity<Object> editData(@RequestBody DataSourceSaveDTO dto);

    @GetMapping("/info/getCurrentUserInfo")
    ResultEntity<Object> getCurrentUserInfo();

    @PostMapping("/sqlFactroy/sqlCheck")
    ResultEntity<List<TableMetaDataObject>> sqlCheck(@RequestParam("sql")String sql, @RequestParam("dbType")String dbType);

    /**
     * 获取单条数据源连接信息
     *
     * @param datasourceId
     * @return
     */
    @GetMapping("/datasource/getById/{datasourceId}")
    ResultEntity<DataSourceDTO> getById(@RequestParam("datasourceId") int datasourceId);

    /**
     * 获取单条数据源连接信息
     *
     * @param ip
     * @param dbName
     * @return
     */
    @GetMapping("/datasource/getByIpAndDbName")
    ResultEntity<DataSourceDTO> getByIpAndDbName(@RequestParam("ip")String ip,@RequestParam("dbName") String dbName);

    /**
     * 根据用户id和页面url查询是否有此页面权限
     * @param userId
     * @param pageUrl
     * @return
     */
    @GetMapping("/info/verifyPageByUserId")
    ResultEntity<Boolean> verifyPageByUserId(@RequestParam("userId") int userId,@RequestParam("pageUrl")String pageUrl);

    /**
     * 获取默认邮件服务器信息
     * @return
     */
    @GetMapping("/emailserver/getDefaultEmailServer")
    ResultEntity<EmailServerVO> getDefaultEmailServer();
}
