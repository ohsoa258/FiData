package com.fisk.dataservice.map;

import com.fisk.dataservice.dto.dataanalysisview.DataViewAccountDTO;
import com.fisk.dataservice.dto.dataanalysisview.DataViewDTO;
import com.fisk.dataservice.dto.dataanalysisview.DataViewFieldsDTO;
import com.fisk.dataservice.dto.dataanalysisview.DataViewThemeDTO;
import com.fisk.dataservice.entity.DataViewAccountPO;
import com.fisk.dataservice.entity.DataViewPO;
import com.fisk.dataservice.entity.DataViewThemePO;
import com.fisk.dataservice.entity.ViewFieldsPO;
import com.fisk.dataservice.vo.dataanalysisview.DataViewThemeVO;
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
public interface DataViewFieldsMap {
    
    DataViewFieldsMap INSTANCES = Mappers.getMapper(DataViewFieldsMap.class);

    /**
     * poList -> dtoList
     *
     * @param subList
     * @return
     */
    List<DataViewFieldsDTO> PoToDtoList(List<ViewFieldsPO> subList);
}
