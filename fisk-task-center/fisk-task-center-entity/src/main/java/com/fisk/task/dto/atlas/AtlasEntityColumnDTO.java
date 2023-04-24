package com.fisk.task.dto.atlas;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author: DennyHui
 * CreateTime: 2021/7/12 14:07
 * Description:
 */
@Data
public class AtlasEntityColumnDTO {
    @ApiModelProperty(value = "列名称")
    public String columnName;
    @ApiModelProperty(value = "列id")
    public long columnId;
    @ApiModelProperty(value = "数据类型")
    public String dataType;
    @ApiModelProperty(value = "是否主键")
    public String isKey;

    @ApiModelProperty(value = "注解")
    public String comment;

    @ApiModelProperty(value = "guid")
    //Atlas回写的时候赋值
    public String guid;
}
