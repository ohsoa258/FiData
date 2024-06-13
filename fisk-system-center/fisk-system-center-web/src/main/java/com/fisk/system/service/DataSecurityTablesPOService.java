package com.fisk.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.system.dto.datasecurity.DataSecurityTablesDTO;
import com.fisk.system.entity.DataSecurityTablesPO;

import java.util.List;

/**
 * @author 56263
 * @description 针对表【tb_data_security_tables】的数据库操作Service
 * @createDate 2024-06-06 14:20:15
 */
public interface DataSecurityTablesPOService extends IService<DataSecurityTablesPO> {

    /**
     * 数据安全 表级安全 批量保存
     *
     * @param dtoList
     * @return
     */
    Object saveTables(List<DataSecurityTablesDTO> dtoList);

    /**
     * 数据安全 表级安全 分页回显
     *
     * @return
     */
    List<DataSecurityTablesDTO> getTables();

    /**
     * 数据安全 表级安全 根据角色id获取该角色的表级安全权限
     *
     * @param roleId
     * @return
     */
    List<DataSecurityTablesDTO> getTablesByRoleId(Integer roleId);

    /**
     * 数据安全 表级安全 单个删除
     *
     * @param id
     * @return
     */
    Object deleteTableSecurityById(Integer id);

    /**
     * 获取所有应用以及表、字段数据
     *
     * @return
     */
    ResultEntity<Object> getAccessAppDetails();
}
