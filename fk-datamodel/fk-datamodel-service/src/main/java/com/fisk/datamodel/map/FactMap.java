package com.fisk.datamodel.map;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datamodel.dto.fact.FactDTO;
import com.fisk.datamodel.dto.fact.FactDropDTO;
import com.fisk.datamodel.dto.fact.FactListDTO;
import com.fisk.datamodel.entity.FactPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FactMap {
    FactMap INSTANCES = Mappers.getMapper(FactMap.class);

    /**
     * dto==>po
     * @param dto
     * @return
     */
    FactPO dtoToPo(FactDTO dto);

    /**
     * po==>dto
     * @param po
     * @return
     */
    FactDTO poToDto(FactPO po);

    /**
     * 分页po==>dto
     * @return
     */
    Page<FactListDTO> pagePoToDto(IPage<FactPO> po);

    /**
     * dropPo==>dto
     * @param po
     * @return
     */
    List<FactDropDTO> dropPoToDto(List<FactPO> po);

}
