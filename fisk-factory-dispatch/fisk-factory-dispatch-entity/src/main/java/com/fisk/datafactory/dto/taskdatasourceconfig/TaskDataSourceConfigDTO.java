package com.fisk.datafactory.dto.taskdatasourceconfig;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class TaskDataSourceConfigDTO {

    @ApiModelProperty(value = "主键id,大于0为编辑,等于=新增", required = true)
    public long id = 0;

    @ApiModelProperty(value = "数据类型", required = true)
    public String type;

    @ApiModelProperty(value = "服务器地址", required = true)
    public String host;

    @ApiModelProperty(value = "端口", required = true)
    public String port;

    @ApiModelProperty(value = "数据库", required = true)
    public String dbName;

    @ApiModelProperty(value = "连接字符串", required = true)
    public String connectStr;

    @ApiModelProperty(value = "连接账号", required = true)
    public String connectAccount;

    @ApiModelProperty(value = "连接密码", required = true)
    public String connectPwd;

    @ApiModelProperty(value = "oracle服务类型：0服务名 1SID", required = false)
    public Integer serviceType;

    @ApiModelProperty(value = "oracle服务名", required = false)
    public String serviceName;

    @ApiModelProperty(value = "组件id", required = true)
    public Integer taskId;

    @ApiModelProperty(value = "域名", required = true)
    public String domainName;

}
