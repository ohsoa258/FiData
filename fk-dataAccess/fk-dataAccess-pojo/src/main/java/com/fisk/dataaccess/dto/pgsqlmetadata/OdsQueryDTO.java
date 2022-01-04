package com.fisk.dataaccess.dto.pgsqlmetadata;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class OdsQueryDTO {
    @ApiModelProperty(value = "当前页")
    public int pageIndex;
    @ApiModelProperty(value = "每页显示条数")
    public int pageSize;
    @ApiModelProperty(value = "应用id", required = true)
    public long appId;
    @ApiModelProperty(value = "SQL脚本or预览的文本全路径", required = true)
    public String querySql;
    @ApiModelProperty(value = "当前物理表名称", required = true)
    public String tableName;
}
