package com.fisk.dataaccess.vo.table;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class PhyTblAndApiTblVO {

    /**
     * 非实时物理表数量
     */
    @ApiModelProperty(value = "非实时物理表数量")
    private Integer phyCount;

    /**
     * 实时api数量
     */
    @ApiModelProperty(value = "实时api数量")
    private Integer apiCount;

}
