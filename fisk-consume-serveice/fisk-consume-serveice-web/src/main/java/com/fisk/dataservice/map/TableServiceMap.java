package com.fisk.dataservice.map;

import com.fisk.dataservice.dto.tableservice.TableServiceDTO;
import com.fisk.dataservice.entity.TableServicePO;
import com.fisk.dataservice.vo.tableservice.TableServiceVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TableServiceMap {

    TableServiceMap INSTANCES = Mappers.getMapper(TableServiceMap.class);

    /**
     * dto==>Po
     *
     * @param dto
     * @return
     */
    TableServicePO dtoToPo(TableServiceDTO dto);

    /**
     * po==>Dto
     *
     * @param po
     * @return
     */
    TableServiceDTO poToDto(TableServicePO po);

    /**
     * poList==>VoList
     *
     * @param poList
     * @return
     */
    List<TableServiceVO> poListToVoList(List<TableServicePO> poList);

}
