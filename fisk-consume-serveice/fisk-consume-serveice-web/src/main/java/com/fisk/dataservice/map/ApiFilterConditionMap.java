package com.fisk.dataservice.map;

import com.fisk.common.core.utils.Dto.SqlWhereDto;
import com.fisk.dataservice.dto.api.FilterConditionConfigDTO;
import com.fisk.dataservice.entity.FilterConditionConfigPO;
import com.fisk.dataservice.vo.api.FilterConditionConfigVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author dick
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ApiFilterConditionMap {

    ApiFilterConditionMap INSTANCES = Mappers.getMapper(ApiFilterConditionMap.class);

    /**
     * list集合 dto -> po
     *
     * @param list source
     * @return target
     */
    List<FilterConditionConfigPO> listDtoToPo (List<FilterConditionConfigDTO> list);


    /**
     * list集合 po -> vo
     *
     * @param list source
     * @return target
     */
    List<FilterConditionConfigVO> listPoToVo(List<FilterConditionConfigPO> list);

    /**
     * list集合 dto -> SqlWhereDto
     *
     * @param list source
     * @return target
     */
    List<SqlWhereDto> listDtoToSqlWhereDto(List<FilterConditionConfigDTO> list);

    /**
     * list集合 po -> SqlWhereDto
     *
     * @param list source
     * @return target
     */
    List<SqlWhereDto> listPoToSqlWhereDto(List<FilterConditionConfigPO> list);
}
