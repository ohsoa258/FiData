package com.fisk.dataservice.map;

import com.fisk.dataservice.dto.ApiConfigureField;
import com.fisk.dataservice.dto.ApiConfigureFieldEditDTO;
import com.fisk.dataservice.dto.ConfigureUserDTO;
import com.fisk.dataservice.dto.UserDTO;
import com.fisk.dataservice.entity.ApiConfigureFieldPO;
import com.fisk.dataservice.entity.ConfigureUserPO;
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

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    ConfigureUserPO dtoToPo(ConfigureUserDTO dto);

    /**
     * po => dto
     *
     * @param dto source
     * @return target
     */
    UserDTO poToDto(ConfigureUserPO dto);
}
