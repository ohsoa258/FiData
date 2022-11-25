package com.fisk.datamodel.map;

import com.fisk.datamodel.dto.customscript.CustomScriptDTO;
import com.fisk.datamodel.dto.customscript.CustomScriptInfoDTO;
import com.fisk.datamodel.entity.CustomScriptPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CustomScriptMap {

    CustomScriptMap INSTANCES = Mappers.getMapper(CustomScriptMap.class);

    /**
     * dto==>Po
     *
     * @param dto
     * @return
     */
    CustomScriptPO dtoToPo(CustomScriptDTO dto);

    /**
     * po==>Dto
     *
     * @param po
     * @return
     */
    CustomScriptDTO poToDto(CustomScriptPO po);

    /**
     * poList==>DtoList
     *
     * @param poList
     * @return
     */
    List<CustomScriptInfoDTO> poListToDtoList(List<CustomScriptPO> poList);

}
