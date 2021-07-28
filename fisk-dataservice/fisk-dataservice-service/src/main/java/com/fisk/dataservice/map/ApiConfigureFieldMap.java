package com.fisk.dataservice.map;

import com.fisk.dataservice.dto.ApiConfigureField;
import com.fisk.dataservice.dto.ApiConfigureFieldEditDTO;
import com.fisk.dataservice.entity.ApiConfigureFieldPO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author wangyan
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ApiConfigureFieldMap {

    ApiConfigureFieldMap INSTANCES = Mappers.getMapper(ApiConfigureFieldMap.class);


    /**
     * dto => PO
     * @param dto
     * @return
     */
    List<ApiConfigureFieldPO> dtoConfigureFieldListPo(List<ApiConfigureField> dto);

    /**
     * editDto => po
     * @param dto source
     * @param po target
     */
    @Mappings({
            @Mapping(target = "id", ignore = true)
    })
    void editDtoToPo(ApiConfigureFieldEditDTO dto, @MappingTarget ApiConfigureFieldPO po);
}
