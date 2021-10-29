package com.fisk.datamodel.service;

import com.fisk.datafactory.dto.components.ChannelDataDTO;
import com.fisk.datafactory.dto.components.NifiComponentsDTO;
import com.fisk.datamodel.dto.dimension.DimensionTabDTO;

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
}
