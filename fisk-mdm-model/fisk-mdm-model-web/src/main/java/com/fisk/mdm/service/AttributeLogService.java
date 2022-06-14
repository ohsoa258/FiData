package com.fisk.mdm.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.attributelog.AttributeLogDTO;
import com.fisk.mdm.dto.attributelog.AttributeLogSaveDTO;
import com.fisk.mdm.dto.attributelog.AttributeLogUpdateDTO;
import com.fisk.mdm.dto.attributelog.AttributeRollbackDTO;

import java.util.List;

/**
 * @Author WangYan
 * @Date 2022/6/14 14:57
 * @Version 1.0
 */
public interface AttributeLogService {

    /**
     * 保存属性日志
     * @param dto
     * @return
     */
    ResultEnum saveAttributeLog(AttributeLogSaveDTO dto);

    /**
     * 删除属性日志(根据属性id)
     * @param attributeId
     * @return
     */
    ResultEnum deleteDataByAttributeId(Integer attributeId);

    /**
     * 删除属性日志(根据id)
     * @param id
     * @return
     */
    ResultEnum deleteData(Integer id);

    /**
     * 查询日志数据(根据属性id)
     * @param attributeId
     * @return
     */
    List<AttributeLogDTO> queryDataByAttributeId(Integer attributeId);

    /**
     * 回滚数据
     * @param dto
     * @return
     */
    ResultEnum rollbackData(AttributeRollbackDTO dto);

    /**
     * 属性日志修改接口
     * @param dto
     * @return
     */
    ResultEnum updateAttributeLog(AttributeLogUpdateDTO dto);
}
