package com.fisk.dataaccess.dto.table;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class TableKeepNumberDTO {

    @ApiModelProperty(value = "id")
    public Long id;
    /**
     * stg数据保留天数
     */

    @ApiModelProperty(value = "stg数据保留天数")
    public String keepNumber;

}
