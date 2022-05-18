package com.fisk.datagovernance.dto.dataquality.datacheck;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验_同步DTO
 * @date 2022/5/16 20:44
 */
@Data
public class DataCheckSyncDTO {
    /**
     * 服务器IP
     */
    @ApiModelProperty(value = "服务器IP")
    @NotNull()
    public String ip;

    /**
     * 数据库名称
     */
    @ApiModelProperty(value = "数据库名称")
    @NotNull()
    public String dbName;

    /**
     * 表名称集合
     */
    @ApiModelProperty(value = "表名称集合")
    @NotNull()
    public List<String> tables;
}
