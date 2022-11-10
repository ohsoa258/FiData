package com.fisk.dataaccess.map;

import com.fisk.dataaccess.dto.output.datatarget.DataTargetAddDTO;
import com.fisk.dataaccess.entity.DataTargetPO;
import com.fisk.dataaccess.vo.output.datatarget.DataTargetVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author JianWenYang
 * @date 2022-08-17 15:28
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DataTargetMap {

    DataTargetMap INSTANCES = Mappers.getMapper(DataTargetMap.class);

    /**
     * dto==>Po
     *
     * @param dto
     * @return
     */
    DataTargetPO dtoToPo(DataTargetAddDTO dto);

    /**
     * po==>vo
     *
     * @param po
     * @return
     */
    DataTargetVO poToVo(DataTargetPO po);

    /**
     * vo==>Po
     *
     * @param vo
     * @return
     */
    DataTargetPO voToPo(DataTargetVO vo);

}
