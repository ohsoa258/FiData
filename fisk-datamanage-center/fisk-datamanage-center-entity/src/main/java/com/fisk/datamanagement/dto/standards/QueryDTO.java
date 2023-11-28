package com.fisk.datamanagement.dto.standards;

import com.fisk.dataaccess.enums.DataSourceTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-11-21
 * @Description:
 */
@Data
public class QueryDTO {

    @ApiModelProperty(value = "当前页")
    public int pageIndex;
    @ApiModelProperty(value = "每页显示条数")
    public int pageSize;
    @ApiModelProperty(value = "数据源id", required = true)
    public Integer dataSourceId;
    @ApiModelProperty(value = "当前库名称", required = true)
    public String dataBaseName;
    @ApiModelProperty(value = "当前表名称", required = true)
    public String tableName;
    @ApiModelProperty(value = "查询语句sql", required = true)
    public String querySql;
}
