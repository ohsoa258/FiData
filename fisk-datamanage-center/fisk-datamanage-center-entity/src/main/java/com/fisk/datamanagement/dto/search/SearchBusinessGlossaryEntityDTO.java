package com.fisk.datamanagement.dto.search;

import io.swagger.annotations.ApiModelProperty;
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

    @ApiModelProperty(value = "对象")
    public List<EntitiesDTO> entities;

    @ApiModelProperty(value = "参数选择")
    public SearchParametersDto searchParameters;

    @ApiModelProperty(value = "查询类型")
    public String queryType;

    @ApiModelProperty(value = "近似合计")
    public String approximateCount;
}
