package com.fisk.dataservice.map;

import com.fisk.dataservice.dto.api.FieldEncryptConfigDTO;
import com.fisk.dataservice.entity.ApiEncryptConfigPO;
import com.fisk.dataservice.entity.FieldConfigPO;
import com.fisk.dataservice.vo.api.FieldConfigVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2024-03-20
 * @Description:
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ApiEncryptConfigMap {

    ApiEncryptConfigMap INSTANCES = Mappers.getMapper(ApiEncryptConfigMap.class);

    /**
     * list集合 dto -> po
     *
     * @param dto source
     * @return target
     */
    ApiEncryptConfigPO dtoToPo(FieldEncryptConfigDTO dto);
}
