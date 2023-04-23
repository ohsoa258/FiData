package com.fisk.datagovernance.dto.dataops;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description PG/SQL SERVICE
 * @date 2022/4/22 13:48
 */
@Data
public class PostgreDTO {
    @ApiModelProperty(value = "id")
    public int id;
    @ApiModelProperty(value = "端口")
    public int port;
    @ApiModelProperty(value = "ip")
    public String ip;
    @ApiModelProperty(value = "数据库名称")
    public String dbName;
    @ApiModelProperty(value = "数据源类型枚举")
    public DataSourceTypeEnum dataSourceTypeEnum;

    @ApiModelProperty(value = "sql路径")
    public String sqlUrl;

    @ApiModelProperty(value = "sql用户名")
    public String sqlUsername;

    @ApiModelProperty(value = "sql密码")
    public String sqlPassword;
}
