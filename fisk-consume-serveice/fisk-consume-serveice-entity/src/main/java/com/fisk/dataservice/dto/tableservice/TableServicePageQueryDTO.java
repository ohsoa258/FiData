package com.fisk.dataservice.dto.tableservice;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class TableServicePageQueryDTO {

    @ApiModelProperty(value = "表名")
    public String tableName;

    @ApiModelProperty(value = "发布状态")
    public Integer publish;

    @ApiModelProperty(value = "表应用ID")
    public Integer tableAppId;

    public Page<TableServicePageDataDTO> page;

}
