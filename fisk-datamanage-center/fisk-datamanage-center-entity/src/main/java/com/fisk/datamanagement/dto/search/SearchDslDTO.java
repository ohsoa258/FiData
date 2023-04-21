package com.fisk.datamanagement.dto.search;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class SearchDslDTO {

    @ApiModelProperty(value = "查询")
    public String query;

    @ApiModelProperty(value = "限制")
    public Integer limit;

    @ApiModelProperty(value = "补偿")
    public Integer offset;

}
