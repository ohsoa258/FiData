package com.fisk.datamodel.map.fact;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datamodel.dto.businessprocess.BusinessProcessDTO;
import com.fisk.datamodel.dto.businessprocess.BusinessProcessDropDTO;
import com.fisk.datamodel.dto.businessprocess.BusinessProcessListDTO;
import com.fisk.datamodel.entity.fact.BusinessProcessPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

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

    /**
     * po==>dropDto
     * @param po
     * @return
     */
    List<BusinessProcessDropDTO> poToDropPo(List<BusinessProcessPO> po);

    /**
     * poList==>DtoList
     * @param po
     * @return
     */
    List<BusinessProcessListDTO> poListToDtoList(List<BusinessProcessPO> po);

}
