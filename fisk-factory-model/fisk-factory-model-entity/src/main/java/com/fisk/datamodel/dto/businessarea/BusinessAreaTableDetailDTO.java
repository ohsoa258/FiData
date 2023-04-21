package com.fisk.datamodel.dto.businessarea;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class BusinessAreaTableDetailDTO {

    @ApiModelProperty(value = "表名称")
    public String tableName;

}
