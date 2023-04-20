package com.fisk.mdm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.access.AccessAttributeAddDTO;
import com.fisk.mdm.dto.access.AccessAttributeListDTO;
import com.fisk.mdm.dto.access.AccessSqlDTO;
import com.fisk.mdm.entity.AccessDataPO;


/**
 * @author wangjian
 * @date 2023-04-18
 * @Description: 数据接入
 */
public interface AccessDataService extends IService<AccessDataPO> {

    AccessAttributeListDTO getAccessAttributeList(Integer moudleId,Integer entityId);

    /**
     * 更新接入脚本数据
     * @param dto
     * @return
     */
    ResultEnum updateAccessSql(AccessSqlDTO dto);

    /**
     * 添加修改接入属性字段
     * @param dto
     * @return
     */
    ResultEnum addOrUpdateAccessAttribute(AccessAttributeAddDTO dto);
}

