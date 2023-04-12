package com.fisk.dataservice.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.dto.tablefields.TableFieldDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface ITableField {

    /**
     * 新增表服务字段数据
     *
     * @param tableServiceId
     * @param fieldDTOList
     * @return
     */
    ResultEnum addTableServiceField(long tableServiceId, List<TableFieldDTO> fieldDTOList);

    /**
     * 删除表服务字段数据
     *
     * @param tableServiceId
     * @param tableFieldId
     * @return
     */
    ResultEnum delTableServiceField(long tableServiceId,long tableFieldId);

    /**
     * 获取表服务字段
     *
     * @param tableServiceId
     * @param tableFieldId
     * @return
     */
    List<TableFieldDTO> getTableServiceField(long tableServiceId,long tableFieldId);

}
