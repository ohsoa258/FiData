package com.fisk.task.dto.nifi;

import com.davis.client.model.PositionDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author gy
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildDbControllerServiceDTO extends BaseProcessorDTO {
    @ApiModelProperty(value = "启用")
    public boolean enabled;
    @ApiModelProperty(value = "con路径")
    public String conUrl;
    @ApiModelProperty(value = "驱动名称")
    public String driverName;
    @ApiModelProperty(value = "驱动地址")
    public String driverLocation;
    @ApiModelProperty(value = "用户")
    public String user;
    @ApiModelProperty(value = "密码")
    public String pwd;
    /**
     * dbcp-max-idle-conns 最大连接数
     */
    @ApiModelProperty(value = "最大连接数")
    public String dbcpMaxIdleConns;
}
