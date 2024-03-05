package com.fisk.task.dto.pgsql;

import com.fisk.common.core.enums.task.BusinessTypeEnum;
import com.fisk.dataaccess.dto.app.AppDataSourceDTO;
import com.fisk.task.dto.MQBaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author: DennyHui
 * CreateTime: 2021/9/15 10:44
 * Description:
 */
@Data
public class PgsqlDelTableDTO extends MQBaseDTO {
    /**
     * 应用id
     */
    @ApiModelProperty(value = "应用id")
    public String appAtlasId;

    @ApiModelProperty(value = "用户Id")
    public Long userId;
    /**
     * 表数组
     */
    @ApiModelProperty(value = "表数组")
    public List<TableListDTO> tableList;

    @ApiModelProperty(value = "删除应用")
    public boolean delApp;

    @ApiModelProperty(value = "业务类型枚举")
    public BusinessTypeEnum businessTypeEnum;

    @ApiModelProperty(value = "应用引用的数据源集合")
    public List<AppDataSourceDTO> appSources;

    /**
     * 应用简称
     */
    @ApiModelProperty(value = "应用简称")
    public String appAbbreviation;

    /**
     * 目标数据库id
     */
    @ApiModelProperty(value = "目标数据库id")
    public Integer targetDbId;

}


