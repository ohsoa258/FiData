package com.fisk.datamodel.dto.businessprocess;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class BusinessProcessPublishDTO {

    /**
     * 业务过程id集合
     */
    @ApiModelProperty(value = "业务过程id集合")
    public List<Integer> businessProcessIds;
    /**
     * 业务域id
     */
    @ApiModelProperty(value = "业务域id")
    public int businessAreaId;
}
