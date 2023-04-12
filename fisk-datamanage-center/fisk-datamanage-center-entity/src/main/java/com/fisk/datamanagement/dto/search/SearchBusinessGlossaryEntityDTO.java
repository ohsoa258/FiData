package com.fisk.datamanagement.dto.search;

import lombok.Data;

import java.util.List;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
@Data
public class SearchBusinessGlossaryEntityDTO {

    public List<EntitiesDTO> entities;

    public SearchParametersDto searchParameters;

    public String queryType;

    public String approximateCount;
}
