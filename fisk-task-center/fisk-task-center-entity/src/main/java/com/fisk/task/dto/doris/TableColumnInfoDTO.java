package com.fisk.task.dto.doris;

import com.fisk.task.dto.MQBaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author DennyHui
 * CreateTime: 2021/7/1 15:08
 * Description:
 */
@Data
public class TableColumnInfoDTO extends MQBaseDTO {
    @ApiModelProperty(value = "列名称")
    public String columnName;
    @ApiModelProperty(value = "是否主键")
    public String isKey;
    @ApiModelProperty(value = "类型")
    public String type;
    @ApiModelProperty(value = "注解")
    public String comment;
}
