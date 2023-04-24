package com.fisk.task.dto.pgsql;

import com.fisk.task.dto.MQBaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author: DennyHui
 * CreateTime: 2021/9/17 18:28
 * Description:
 */
@Data
public class TableListDTO extends MQBaseDTO {

    @ApiModelProperty(value = "表名")
    public String tableName;
    @ApiModelProperty(value = "表地图集id")
    public String tableAtlasId;
}
