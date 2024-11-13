package com.fisk.dataaccess.dto.datamodel;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class AppRegistrationDataDTO {

    @ApiModelProperty(value = "id")
    public long id;
    @ApiModelProperty(value = "app名称")
    public String appName;
    @ApiModelProperty(value = "应用缩写")
    public String appAbbreviation;
    @ApiModelProperty(value = "whetherSchema")
    public boolean whetherSchema;
    @ApiModelProperty(value = "目标数据库ID")
    public Integer targetDbId;
    /**
     * 应用类型: 0: 实时应用   1: 非实时应用  2:CDC接入  3:JDBC接入
     */
    @ApiModelProperty(value = "应用类型")
    public int appType;
    @ApiModelProperty(value = "表Dto列表")
    public List<TableAccessDataDTO> tableDtoList;

}
