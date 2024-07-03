package com.fisk.datamodel.map.fact;

import com.fisk.datamodel.dto.atomicindicator.AtomicIndicatorFactAttributeDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeDataDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeDropDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeUpdateDTO;
import com.fisk.datamodel.entity.fact.FactAttributePO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FactAttributeMap {
    FactAttributeMap INSTANCES = Mappers.getMapper(FactAttributeMap.class);

    /**
     * dto==>po
     * @param dto
     * @return
     */
    @Mapping(target = "updateUser", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "delFlag", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    FactAttributePO dtoToPo(FactAttributeDTO dto);

    /**
     * po -> dto
     *
     * @param po source
     * @return target
     */
    @Mapping(target = "associatedDto", ignore = true)
    FactAttributeDTO poToDto(FactAttributePO po);

    /**
     * poDrop==>dto
     * @param po
     * @return
     */
    List<FactAttributeDropDTO> poDropToDto(List<FactAttributePO> po);

    /**
     * poList==>dtoList
     * @param po
     * @return
     */
    List<FactAttributeDataDTO> poListToDtoList(List<FactAttributePO> po);

    /**
     * addDto==>PoList
     * @param dto
     * @return
     */
    List<FactAttributePO> addDtoToPoList(List<FactAttributeDTO> dto);

    /**
     * poLists==>DtoList
     * @param po
     * @return
     */
    List<FactAttributeDTO> poListsToDtoList(List<FactAttributePO> po);

    /**
     * poDetail==>Dto
     * @param po
     * @return
     */
    @Mapping(target = "execSql", ignore = true)
    @Mapping(target = "deltaTimes", ignore = true)
    FactAttributeUpdateDTO poDetailToDto(FactAttributePO po);

    /**
     * attributePo==>Dto
     * @param po
     * @return
     */
    List<AtomicIndicatorFactAttributeDTO> attributePoToDto(List<FactAttributePO> po);

    /**
     * poDetail==>DtoList
     * @param po
     * @return
     */
    List<FactAttributeUpdateDTO> poDetailToDtoList(List<FactAttributePO> po);

}
