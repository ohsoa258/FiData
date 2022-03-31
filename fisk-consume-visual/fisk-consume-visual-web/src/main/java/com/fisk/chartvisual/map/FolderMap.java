package com.fisk.chartvisual.map;

import com.fisk.chartvisual.dto.FolderDTO;
import com.fisk.chartvisual.dto.FolderEditDTO;
import com.fisk.chartvisual.entity.FolderPO;
import com.fisk.chartvisual.vo.FolderVO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author gy
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FolderMap {

    FolderMap INSTANCES = Mappers.getMapper(FolderMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    FolderPO dtoToPo(FolderDTO dto);

    /**
     * editDto => po
     *
     * @param dto source
     * @param po target
     */
    @Mappings({
            @Mapping(target = "id", ignore = true)
    })
    void editDtoToPo(FolderEditDTO dto, @MappingTarget FolderPO po);

    /**
     * po => vo
     *
     * @param list po
     * @return vo
     */
    @Mappings({
            @Mapping(target = "child", ignore = true),
    })
    List<FolderVO> poToVo(List<FolderPO> list);
}
