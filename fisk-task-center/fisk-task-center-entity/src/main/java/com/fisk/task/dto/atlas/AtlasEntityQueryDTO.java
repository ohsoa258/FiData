package com.fisk.task.dto.atlas;

import com.fisk.task.dto.MQBaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author: DennyHui
 * CreateTime: 2021/7/14 12:58
 * Description:
 */
@Data
public class AtlasEntityQueryDTO extends MQBaseDTO {
    @ApiModelProperty(value = "应用Id")
    public String appId;

    @ApiModelProperty(value = "数据库Id")
    public String dbId;

    @ApiModelProperty(value = "表名称")
    public String tableName;
}
