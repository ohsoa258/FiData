package com.fisk.system.service;

import com.fisk.system.dto.datasecurity.DataSecurityColumnsDTO;
import com.fisk.system.entity.DataSecurityColumnsPO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 56263
* @description 针对表【tb_data_security_columns】的数据库操作Service
* @createDate 2024-06-06 14:20:15
*/
public interface DataSecurityColumnsPOService extends IService<DataSecurityColumnsPO> {

    /**
     * 数据安全 列级安全 批量保存
     *
     * @param dtoList
     * @return
     */
    Object saveColumns(List<DataSecurityColumnsDTO> dtoList);

    /**
     * 数据安全 列级安全 回显
     *
     * @return
     */
    List<DataSecurityColumnsDTO> getColumns();

    /**
     * 数据安全 列级安全 单个删除
     *
     * @param id
     * @return
     */
    Object deleteColumnSecurityById(Integer id);

    /**
     * 数据安全 列级安全 根据角色id获取该角色的列级安全权限
     *
     * @param roleId
     * @return
     */
    List<DataSecurityColumnsDTO> getColumnsByRoleId(Integer roleId);

}
