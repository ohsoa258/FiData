package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

/**
 * @author wangjian
 * @date 2024-10-14 14:17:55
 */
@TableName("tb_datacheck_server_app_config")
@Data
public class DatacheckServerAppConfigPO extends BasePO {

    @ApiModelProperty(value = "应用名称")
    public String appName;
    @ApiModelProperty(value = "应用描述")
    public String appDesc;
    @ApiModelProperty(value = "申请人")
    public String appPrincipal;
    @ApiModelProperty(value = "账号")
    public String appAccount;
    @ApiModelProperty(value = "密码/MD5加密存储")
    public String appPassword;
}
