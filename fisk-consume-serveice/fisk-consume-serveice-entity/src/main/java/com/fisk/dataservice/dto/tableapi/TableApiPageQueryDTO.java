package com.fisk.dataservice.dto.tableapi;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.dataservice.dto.tableservice.TableServicePageDataDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-09-12
 * @Description:
 */
@Data
public class TableApiPageQueryDTO {
    @ApiModelProperty(value = "api名")
    public String apiName;

    @ApiModelProperty(value = "发布状态")
    public Integer publish;

    @ApiModelProperty(value = "表应用ID")
    public Integer appId;

    @ApiModelProperty(value = "页")
    public Page<TableApiPageDataDTO> page;
}
