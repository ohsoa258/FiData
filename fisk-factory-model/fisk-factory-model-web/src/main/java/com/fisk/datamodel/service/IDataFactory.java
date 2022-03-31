package com.fisk.datamodel.service;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.dataaccess.dto.taskschedule.ComponentIdDTO;
import com.fisk.dataaccess.dto.taskschedule.DataAccessIdsDTO;
import com.fisk.datafactory.dto.components.ChannelDataDTO;
import com.fisk.datafactory.dto.components.NifiComponentsDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IDataFactory {

    /**
     * 获取维度表id以及名称集合
     * @param dto
     * @return
     */
    List<ChannelDataDTO> getTableIds(NifiComponentsDTO dto);

    /**
     * 根据业务域id与表id获取业务域名称/表名称
     * @param dto
     * @return
     */
    ResultEntity<ComponentIdDTO> getBusinessAreaNameAndTableName(DataAccessIdsDTO dto);

}
