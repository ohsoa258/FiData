package com.fisk.datamodel.map.widetable;

import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceFieldConfigDTO;
import com.fisk.datamodel.dto.widetablefieldconfig.WideTableFieldConfigsDTO;
import com.fisk.datamodel.entity.widetable.WideTableFieldConfigPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface WideTableFieldConfigMap {

    WideTableFieldConfigMap INSTANCES = Mappers.getMapper(WideTableFieldConfigMap.class);

    /**
     * dtoList==>PoList
     *
     * @param dtoList
     * @return
     */
    List<WideTableFieldConfigPO> dtoListToPoList(List<WideTableFieldConfigsDTO> dtoList);

    /**
     * poList==>DtoList
     *
     * @param poList
     * @return
     */
    List<WideTableFieldConfigsDTO> poListToDtoList(List<WideTableFieldConfigPO> poList);

    /**
     * source==>Dto
     *
     * @param dto
     * @return
     */
    WideTableFieldConfigsDTO sourceToDto(TableSourceFieldConfigDTO dto);


}
