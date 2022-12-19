package com.fisk.dataservice.dto.datasource;

import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DataSourceQueryDTO {

    @ApiModelProperty(value = "当前页")
    public int pageIndex;
    @ApiModelProperty(value = "每页显示条数")
    public int pageSize;
    @ApiModelProperty(value = "SQL脚本or预览的文本全路径", required = true)
    public String querySql;
    /*
     * 接入的增量时间参数
     */
    public List<DeltaTimeDTO> deltaTimes;
    @ApiModelProperty(value = "库名", required = true)
    public String dbName;
    @ApiModelProperty(value = "表名", required = true)
    public String tableName;

}
