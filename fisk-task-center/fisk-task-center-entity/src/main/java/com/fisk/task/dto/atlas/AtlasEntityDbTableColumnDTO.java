package com.fisk.task.dto.atlas;

import com.fisk.task.dto.MQBaseDTO;
import com.fisk.task.enums.DbTypeEnum;
import com.fisk.task.enums.OdsDataSyncTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author: DennyHui
 * CreateTime: 2021/7/12 13:59
 * Description:
 */
@Data
public class AtlasEntityDbTableColumnDTO extends MQBaseDTO {
    @ApiModelProperty(value = "数据库ID")
    public String dbId;
    /**
     * 数据库类型
     */
    @ApiModelProperty(value = "数据库类型")
    public DbTypeEnum dbType;
    @ApiModelProperty(value = "表名")
    public String tableName;
    /**
     * 应用简称
     */
    @ApiModelProperty(value = "应用简称")
    public String appAbbreviation;
    @ApiModelProperty(value = "表Id")
    public String tableId;
    /**
     * 数据同步类型；全量、增量
     */
    @ApiModelProperty(value = "数据同步类型；全量、增量")
    public OdsDataSyncTypeEnum syncType;
    /**
     * 增量时间戳字段
     */
    @ApiModelProperty(value = "增量时间戳字段")
    public String syncField;
    /**
     * Corn表达式
     */
    @ApiModelProperty(value = "Corn表达式")
    public String cornExpress;
    @ApiModelProperty(value = "创建者")

    public String createUser;
    @ApiModelProperty(value = "列")
    public List<AtlasEntityColumnDTO> columns;
}
