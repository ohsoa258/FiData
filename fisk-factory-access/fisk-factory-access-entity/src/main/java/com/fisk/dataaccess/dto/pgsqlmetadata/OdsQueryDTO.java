package com.fisk.dataaccess.dto.pgsqlmetadata;

import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import com.fisk.dataaccess.enums.DataSourceTypeEnum;
import com.fisk.dataaccess.enums.FtpFileTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class OdsQueryDTO {
    @ApiModelProperty(value = "当前页")
    public int pageIndex;
    @ApiModelProperty(value = "每页显示条数")
    public int pageSize;
    @ApiModelProperty(value = "应用id", required = false)
    public long appId;
    @ApiModelProperty(value = "关联的数据接入中对应的目标数据源id", required = true)
    public Integer dataSourceId;
    @ApiModelProperty(value = "SQL脚本or预览的文本全路径", required = true)
    public String querySql;
    @ApiModelProperty(value = "预览的起始行", required = true)
    public Integer startRow;

    @ApiModelProperty(value = "当前物理表名称", required = true)
    public String tableName;
    @ApiModelProperty(value = "ftp文件后缀名类型", required = true)
    public FtpFileTypeEnum fileTypeEnum;
    @ApiModelProperty(value = "数据源类型", required = true)
    public DataSourceTypeEnum dataSourceTypeEnum;
    @ApiModelProperty(value = "数据源id", required = true)
    public int appDataSourceId;

    /*
     * 接入的增量时间参数
     */
    public List<DeltaTimeDTO> deltaTimes;
}
