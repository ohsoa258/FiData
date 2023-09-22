package com.fisk.task.dto.task;

import com.fisk.common.core.enums.task.SynchronousTypeEnum;
import com.fisk.dataservice.dto.tablefields.TableFieldDTO;
import com.fisk.dataservice.dto.tablesyncmode.TableSyncModeDTO;
import com.fisk.task.dto.MQBaseDTO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class BuildTableApiServiceDTO extends MQBaseDTO {

    /**
     * 表id
     */
    @ApiModelProperty(value = "apiId")
    public long id;
    /**
     * 表名
     */
    @ApiModelProperty(value = "api名称")
    public String apiName;
    /**
     * 表名
     */
    @ApiModelProperty(value = "api描述")
    public String apiDes;

    /**
     * 同步配置
     */
    @ApiModelProperty(value = "同步配置")
    public TableSyncModeDTO syncModeDTO;

    /**
     * 数据类别
     */
    @ApiModelProperty(value = "数据类别")
    public DataClassifyEnum dataClassifyEnum = DataClassifyEnum.DATA_SERVICE_API;

    /**
     * 表类别
     */
    @ApiModelProperty(value = "表类别")
    public OlapTableEnum olapTableEnum = OlapTableEnum.DATA_SERVICE_API;
    /**
     * 表应用ID
     */
    @ApiModelProperty(value = "表应用ID")
    public Integer appId;
    /**
     * 表应用名称
     */
    @ApiModelProperty(value = "表应用名称")
    public String appName;
    /**
     * 表应用描述
     */
    @ApiModelProperty(value = "表应用描述")
    public String appDesc;

    /**
     * 启用或禁用
     */
    @ApiModelProperty(value = "启用或禁用")
    public Integer enable;
}
