package com.fisk.dataservice.map;

import com.fisk.common.utils.Dto.SqlParmDto;
import com.fisk.dataservice.dto.api.ParmConfigDTO;
import com.fisk.dataservice.dto.api.ParmConfigEditDTO;
import com.fisk.dataservice.entity.ParmConfigPO;
import com.fisk.dataservice.vo.api.ParmConfigVO;
import com.fisk.dataservice.vo.app.AppApiParmVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author dick
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ApiParmMap {

    ApiParmMap INSTANCES = Mappers.getMapper(ApiParmMap.class);

    /**
     * list集合 po -> vo
     *
     * @param list source
     * @return target
     */
    List<AppApiParmVO> listPoToAppApiParmVo(List<ParmConfigPO> list);

    /**
     * list集合 dto -> po
     *
     * @param list source
     * @return target
     */
    List<ParmConfigPO> listDtoToPo(List<ParmConfigDTO> list);

    /**
     * list集合 dto -> po
     *
     * @param list source
     * @return target
     */
    List<ParmConfigPO> listDtoToPo_Edit(List<ParmConfigEditDTO> list);

    /**
     * list集合 po -> vo
     *
     * @param list source
     * @return target
     */
    List<ParmConfigVO> listPoToVo(List<ParmConfigPO> list);

    /**
     * list集合 dto -> SqlParmDto
     *
     * @param list source
     * @return target
     */
    List<SqlParmDto> listDtoToSqlParmDto(List<ParmConfigEditDTO> list);

    /**
     * list集合 po -> SqlParmDto
     *
     * @param list source
     * @return target
     */
    List<SqlParmDto> listPoToSqlParmDto(List<ParmConfigPO> list);
}
