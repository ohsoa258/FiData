package com.fisk.datamodel.map;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datamodel.dto.businessprocess.BusinessProcessDTO;
import com.fisk.datamodel.entity.BusinessProcessPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BusinessProcessMap {
    BusinessProcessMap INSTANCES= Mappers.getMapper(BusinessProcessMap.class);

    /**
     * 分页po==>dto
     * @param po
     * @return 分页结果
     */
    Page<BusinessProcessDTO> pagePoToDto(IPage<BusinessProcessPO> po);

    /**
     * dto==>po
     * @param dto
     * @return 添加结果
     */
    BusinessProcessPO dtoToPo(BusinessProcessDTO dto);

}
