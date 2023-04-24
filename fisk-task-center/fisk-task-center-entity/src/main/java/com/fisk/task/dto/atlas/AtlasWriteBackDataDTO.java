package com.fisk.task.dto.atlas;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author: DennyHui
 * CreateTime: 2021/7/13 13:39
 * Description:
 */
@Data
public class AtlasWriteBackDataDTO {
    @ApiModelProperty(value = "应用Id")
    public String appId;

    @ApiModelProperty(value = "表Id")
    public String tableId;
    @ApiModelProperty(value = "地图集表Id")
    public String atlasTableId;

    @ApiModelProperty(value = "表名")
    public String tableName;

    @ApiModelProperty(value = "用户id")
    public String userId;
    @ApiModelProperty(value = "dorisSelectSqlStr")
    public String dorisSelectSqlStr;
    @ApiModelProperty(value = "列键")
    public List<AtlasEntityColumnDTO> columnsKeys;
}
