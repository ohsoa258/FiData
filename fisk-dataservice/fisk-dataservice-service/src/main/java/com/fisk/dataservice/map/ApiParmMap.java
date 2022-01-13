package com.fisk.dataservice.map;

import com.fisk.dataservice.entity.ParmConfigPO;
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
    List<AppApiParmVO> listPoToVo(List<ParmConfigPO> list);
}
