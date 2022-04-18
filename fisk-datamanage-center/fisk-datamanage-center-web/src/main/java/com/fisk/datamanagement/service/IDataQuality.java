package com.fisk.datamanagement.service;

import com.fisk.datamanagement.dto.dataquality.DataQualityDTO;
import com.fisk.datamanagement.dto.dataquality.UpperLowerBloodParameterDTO;

/**
 * @author JianWenYang
 */
public interface IDataQuality {

    /**
     * 是否存在atlas
     * @param dto
     * @return
     */
    boolean existAtlas(DataQualityDTO dto);

    /**
     * 上下血缘是否存在
     * @param dto
     * @return
     */
    boolean existUpperLowerBlood(UpperLowerBloodParameterDTO dto);

}
