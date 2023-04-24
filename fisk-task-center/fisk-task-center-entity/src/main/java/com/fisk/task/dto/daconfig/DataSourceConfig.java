package com.fisk.task.dto.daconfig;

import com.fisk.common.core.enums.task.nifi.DriverTypeEnum;
import com.fisk.dataaccess.dto.table.TableFieldsDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author gy
 */
@Data
public class DataSourceConfig {
    @ApiModelProperty(value = "组件Id")
    public String componentId;
    @ApiModelProperty(value = "jdbcStr")
    public String jdbcStr;
    @ApiModelProperty(value = "类型")
    public DriverTypeEnum type;
    @ApiModelProperty(value = "用户")
    public String user;
    @ApiModelProperty(value = "密码")
    public String password;
    @ApiModelProperty(value = "同步模式")
    public int syncMode;
    @ApiModelProperty(value = "目标表名")
    public String targetTableName;
    /**
     * 物理表字段
     */
    @ApiModelProperty(value = "物理表字段")
    public List<TableFieldsDTO> tableFieldsList;
}
