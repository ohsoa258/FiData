package com.fisk.dataservice.map;

import com.fisk.dataservice.dto.dataanalysisview.DataViewAccountDTO;
import com.fisk.dataservice.dto.dataanalysisview.DataViewThemeDTO;
import com.fisk.dataservice.entity.DataViewAccountPO;
import com.fisk.dataservice.entity.DataViewThemePO;
import com.fisk.dataservice.entity.FieldConfigPO;
import com.fisk.dataservice.vo.api.FieldConfigVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DataViewMap {
    
    DataViewMap INSTANCES = Mappers.getMapper(DataViewMap.class);

    /**
     * dto -> po
     *
     * @param dto source
     * @return target
     */
    DataViewThemePO dtoToPo(DataViewThemeDTO dto);

    /**
     * po -> dto
     *
     * @param records
     * @return
     */
    List<DataViewThemeDTO> poToDto(List<DataViewThemePO> records);

    /**
     * poList -> dtoList
     *
     * @param subList
     * @return
     */
    List<DataViewAccountDTO> accountListPoToDto(List<DataViewAccountPO> subList);
}
