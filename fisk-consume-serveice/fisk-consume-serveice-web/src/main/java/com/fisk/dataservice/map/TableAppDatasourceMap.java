package com.fisk.dataservice.map;

import com.fisk.dataservice.dto.tableservice.TableAppDatasourceDTO;
import com.fisk.dataservice.entity.TableAppDatasourcePO;
import com.fisk.dataservice.vo.tableservice.TableAppDatasourceVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TableAppDatasourceMap {
    TableAppDatasourceMap INSTANCES = Mappers.getMapper(TableAppDatasourceMap.class);

    /**
     * list集合 po -> vo
     *
     * @param list source
     * @return target
     */
    List<TableAppDatasourceVO> listPoToVo(List<TableAppDatasourcePO> list);

    /**
     * list集合 dto -> po
     *
     * @param list source
     * @return target
     */
    List<TableAppDatasourcePO> listDtoToPo(List<TableAppDatasourceDTO> list);
}
