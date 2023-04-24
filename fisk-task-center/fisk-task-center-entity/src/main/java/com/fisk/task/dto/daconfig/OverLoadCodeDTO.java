package com.fisk.task.dto.daconfig;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.task.SynchronousTypeEnum;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class OverLoadCodeDTO {

    @ApiModelProperty(value = "配置")
    public DataAccessConfigDTO config;

    @ApiModelProperty(value = "同步类型枚举")
    public SynchronousTypeEnum synchronousTypeEnum;

    @ApiModelProperty(value = "函数名")
    public String funcName;
    @ApiModelProperty(value = "构建nifi流")
    public BuildNifiFlowDTO buildNifiFlow;
    @ApiModelProperty(value = "数据源类型")
    public DataSourceTypeEnum dataSourceType;

}
