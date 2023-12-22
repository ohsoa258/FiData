package com.fisk.dataaccess.vo;

import com.fisk.dataaccess.enums.DataSourceTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-12-21
 * @Description:
 */
@Data
public class CDCAppNameAndTableVO {
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "应用名称")
    private String appName;
    @ApiModelProperty(value = "库类型")
    private String dbType;
    private List<TableDbNameAndNameVO> tableDbNameAndNameVO;
}
