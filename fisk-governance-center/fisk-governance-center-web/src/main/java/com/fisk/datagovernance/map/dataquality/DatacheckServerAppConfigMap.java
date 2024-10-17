package com.fisk.datagovernance.map.dataquality;

import com.fisk.common.service.metadata.dto.metadata.MetaDataApplicationDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.AppRegisterDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.AppRegisterEditDTO;
import com.fisk.datagovernance.entity.dataquality.DatacheckServerAppConfigPO;
import com.fisk.datagovernance.util.TypeConversionUtils;
import com.fisk.datagovernance.vo.dataquality.datacheck.AppRegisterVO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2024-10-14
 * @Description:
 */
@Mapper(uses = { TypeConversionUtils.class } ,nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DatacheckServerAppConfigMap {

    DatacheckServerAppConfigMap INSTANCES = Mappers.getMapper(DatacheckServerAppConfigMap.class);


    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    DatacheckServerAppConfigPO dtoToPo(AppRegisterDTO dto);

    /**
     * editDto => po
     *
     * @param dto source
     * @param po target
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "appAccount", ignore = true),
            @Mapping(target = "appPassword", ignore = true)// 添加了ignore，表示不会对该属性做映射
    })
    void editDtoToPo(AppRegisterEditDTO dto, @MappingTarget DatacheckServerAppConfigPO po);

    MetaDataApplicationDTO poDtoMetaDataApplicationDto(DatacheckServerAppConfigPO po);

    List<MetaDataApplicationDTO> poDtoMetaDataApplicationDtoList(List<DatacheckServerAppConfigPO> po);

    /**
     * list集合 po -> vo
     *
     * @param list source
     * @return target
     */
    List<AppRegisterVO> listPoToVo(List<DatacheckServerAppConfigPO> list);
}
