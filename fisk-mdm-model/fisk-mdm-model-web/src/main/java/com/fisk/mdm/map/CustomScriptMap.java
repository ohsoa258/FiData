package com.fisk.mdm.map;

import com.fisk.mdm.dto.access.CustomScriptDTO;
import com.fisk.mdm.dto.access.CustomScriptInfoDTO;
import com.fisk.mdm.entity.CustomScriptPO;
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

    /**
     * dtoList==>PoList
     *
     * @param dtoList
     * @return
     */
    List<CustomScriptPO> dtoListToPoList(List<CustomScriptDTO> dtoList);

}
