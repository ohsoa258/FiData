package com.fisk.task.dto.mdmconfig;

import com.fisk.common.core.enums.task.nifi.DriverTypeEnum;
import com.fisk.task.dto.accessmdm.AccessAttributeDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author wangjian
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
     * mdm表字段
     */
    @ApiModelProperty(value = "mdm表字段")
    public List<AccessAttributeDTO> tableFieldsList;
}
