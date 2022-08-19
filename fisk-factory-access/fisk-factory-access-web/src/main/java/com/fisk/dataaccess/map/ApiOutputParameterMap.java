package com.fisk.dataaccess.map;

import com.fisk.dataaccess.dto.apioutputparameter.ApiOutputParameterDTO;
import com.fisk.dataaccess.entity.ApiOutputParameterPO;
import com.fisk.dataaccess.vo.output.apioutputparameter.ApiOutputParameterVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ApiOutputParameterMap {

    ApiOutputParameterMap INSTANCES = Mappers.getMapper(ApiOutputParameterMap.class);

    /**
     * dtoList==>PoList
     *
     * @param dtoList
     * @return
     */
    List<ApiOutputParameterPO> dtoListToPoList(List<ApiOutputParameterDTO> dtoList);

    /**
     * poList==>VoList
     *
     * @param poList
     * @return
     */
    List<ApiOutputParameterVO> poListToVoList(List<ApiOutputParameterPO> poList);

    /**
     * voList==>DtoList
     *
     * @param voList
     * @return
     */
    List<ApiOutputParameterDTO> voListToDtoList(List<ApiOutputParameterVO> voList);


}
