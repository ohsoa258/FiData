package com.fisk.datagovernance.service.datasecurity;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.entity.datasecurity.PermissionManagementPO;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-29 17:59:34
 */
public interface PermissionManagementService extends IService<PermissionManagementPO> {

    /**
     * 回显: 根据id查询数据
     *
     * @param id id
     * @return 查询结果
     */
    PermissionManagementPO getData(long id);

    /**
     * 添加
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum addData(PermissionManagementPO dto);

    /**
     * 修改
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum editData(PermissionManagementPO dto);

    /**
     * 删除
     *
     * @param id id
     * @return 执行结果
     */
    ResultEnum deleteData(long id);

}

