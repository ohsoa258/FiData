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
    @ApiModelProperty(value = "sapbw-mdx语句集合", required = true)
    public List<String> mdxList;
    @ApiModelProperty(value = "pbi数据集id")
    public String datasetid;
    @ApiModelProperty(value = "pbi用户名")
    public String impersonatedUserName;

    /*
     * 接入的增量时间参数
     */
    @ApiModelProperty(value = "接入的增量时间参数")
    public List<DeltaTimeDTO> deltaTimes;

    /**
     * mongo查询bson字符串
     * 举例:{"username": "Tom"}
     */
    @ApiModelProperty(value = "mongo查询bson字符串")
    public String mongoQueryCondition;

    /**
     * mongo指定返回字段
     * 举例:{"_id": 1, "username": 1, "product": 1, "price": 1, "type": 1}
     */
    @ApiModelProperty(value = "mongo指定返回字段")
    public String mongoNeededFileds;

    /**
     * 对应的mongodb集合名称
     */
    @ApiModelProperty(value = "对应的mongodb集合名称")
    public String mongoCollectionName;

    /**
     * 如果应用选择的数据源类型是mongodb，请传递物理表id
     */
    @ApiModelProperty(value = "如果应用选择的数据源类型是mongodb，请传递物理表id")
    public Long tblId;

}
