package com.fisk.system.service;

import com.fisk.system.dto.datasecurity.DataSecurityRowsDTO;
import com.fisk.system.entity.DataSecurityRowsPO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 56263
* @description 针对表【tb_data_security_rows】的数据库操作Service
* @createDate 2024-06-06 14:20:15
*/
public interface DataSecurityRowsPOService extends IService<DataSecurityRowsPO> {

    /**
     * 数据安全 行级安全 批量保存
     *
     * @param dtoList
     * @return
     */
    Object saveRows(List<DataSecurityRowsDTO> dtoList);

    /**
     * 数据安全 行级安全 回显
     *
     * @return
     */
    Object getRows();

    /**
     * 数据安全 行级安全 单个删除
     *
     * @param id
     * @return
     */
    Object deleteRowSecurityById(Integer id);

    /**
     * 数据安全 行级安全 根据角色id获取该角色的行级安全权限
     *
     * @param roleId
     * @return
     */
    List<DataSecurityRowsDTO> getRowsByRoleId(Integer roleId);

}
