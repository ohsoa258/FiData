package com.fisk.dataservice.map;

import com.fisk.common.service.metadata.dto.metadata.MetaDataApplicationDTO;
import com.fisk.dataservice.dto.app.*;
import com.fisk.dataservice.entity.AppConfigPO;
import com.fisk.dataservice.vo.app.AppRegisterVO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author dick
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AppRegisterMap {

    AppRegisterMap INSTANCES = Mappers.getMapper(AppRegisterMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    AppConfigPO dtoToPo(AppRegisterDTO dto);

    /**
     * editDto => po
     *
     * @param dto source
     * @param po target
     */
    @Mappings({
            @Mapping(target = "id", ignore = true)// 添加了ignore，表示不会对该属性做映射
    })
    void editDtoToPo(AppRegisterEditDTO dto, @MappingTarget AppConfigPO po);

    MetaDataApplicationDTO poDtoMetaDataApplicationDto(AppConfigPO po);

    List<MetaDataApplicationDTO> poDtoMetaDataApplicationDtoList(List<AppConfigPO> po);

    /**
     * list集合 po -> vo
     *
     * @param list source
     * @return target
     */
    List<AppRegisterVO> listPoToVo(List<AppConfigPO> list);
}
