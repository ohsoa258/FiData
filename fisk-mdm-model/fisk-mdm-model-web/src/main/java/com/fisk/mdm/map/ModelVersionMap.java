package com.fisk.mdm.map;

import com.fisk.mdm.dto.modelVersion.ModelCopyDTO;
import com.fisk.mdm.dto.modelVersion.ModelVersionDTO;
import com.fisk.mdm.dto.modelVersion.ModelVersionUpdateDTO;
import com.fisk.mdm.entity.ModelVersionPO;
import com.fisk.mdm.utlis.TypeConversionUtils;
import com.fisk.mdm.vo.modelVersion.ModelVersionDropDownVO;
import com.fisk.mdm.vo.modelVersion.ModelVersionVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author chenYa
 */
@Mapper( uses = { TypeConversionUtils.class } , nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ModelVersionMap {
    ModelVersionMap INSTANCES = Mappers.getMapper(ModelVersionMap.class);

    /**
     * dto => po
     * @param dto
     * @return
     */
    @Mappings({
            @Mapping(source = "status" ,target = "status"),
            @Mapping(source = "type" ,target = "type")
    })
    ModelVersionPO dtoToPo(ModelVersionDTO dto);

    /**
     * dto => po
     * @param dto
     * @return
     */
    @Mappings({
            @Mapping(source = "status" ,target = "status"),
            @Mapping(source = "type" ,target = "type")
    })
    ModelVersionPO updateDtoToPo(ModelVersionUpdateDTO dto);

    /**
     * dto => po
     * @param dto
     * @return
     */
    ModelVersionPO copyDtoToPo(ModelCopyDTO dto);

    /**
     * po => vo
     * @param poList
     * @return
     */
    List<ModelVersionVO> poToVoList(List<ModelVersionPO> poList);

    /**
     * poList==>DropDownVoList
     * @param poList
     * @return
     */
    List<ModelVersionDropDownVO> poListToDropDownVoList(List<ModelVersionPO> poList);

}
