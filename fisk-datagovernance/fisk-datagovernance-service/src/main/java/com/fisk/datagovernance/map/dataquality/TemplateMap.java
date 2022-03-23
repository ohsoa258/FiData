package com.fisk.datagovernance.map.dataquality;

import com.fisk.datagovernance.entity.dataquality.TemplatePO;
import com.fisk.datagovernance.vo.dataquality.template.TemplateVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 模板map
 * @date 2022/3/23 12:30
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TemplateMap {
    TemplateMap INSTANCES = Mappers.getMapper(TemplateMap.class);

    /**
     * list集合 po -> vo
     *
     * @param list source
     * @return target
     */
//    @Mappings({
//            @Mapping(source = "templateModules", target = "templateModules.value"),
//            @Mapping(source = "templateType", target = "templateType.value")
//    })
//    List<TemplateVO> listPoToVo(List<TemplatePO> list);
}
