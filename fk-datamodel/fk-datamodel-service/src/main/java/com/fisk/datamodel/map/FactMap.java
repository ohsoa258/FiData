package com.fisk.datamodel.map;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datamodel.dto.fact.FactDTO;
import com.fisk.datamodel.entity.FactPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FactMap {
    FactMap INSTANCES= Mappers.getMapper(FactMap.class);

    /**
     * 分页po==>dto
     * @param po
     * @return 分页结果
     */
    Page<FactDTO> pagePoToDto(IPage<FactPO> po);

    /**
     * dto==>po
     * @param dto
     * @return 添加结果
     */
    FactPO dtoToPo(FactDTO dto);

}
