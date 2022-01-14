package com.fisk.dataservice.map;

import com.fisk.dataservice.dto.api.FieldConfigEditDTO;
import com.fisk.dataservice.entity.FieldConfigPO;
import com.fisk.dataservice.vo.api.FieldConfigVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author dick
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ApiFieldMap {

    ApiFieldMap INSTANCES = Mappers.getMapper(ApiFieldMap.class);

    /**
     * list集合 po -> vo
     *
     * @param list source
     * @return target
     */
    List<FieldConfigVO> listPoToVo(List<FieldConfigPO> list);

    /**
     * list集合 dto -> po
     *
     * @param list source
     * @return target
     */
    List<FieldConfigPO> listDtoToPo(List<FieldConfigEditDTO> list);
}
