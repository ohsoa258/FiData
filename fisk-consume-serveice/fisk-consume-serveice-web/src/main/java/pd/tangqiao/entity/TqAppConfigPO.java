package pd.tangqiao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

/**
 * @author wangjian
 * @date 2024-10-29 13:42:34
 */
@TableName("tq_app_config")
@Data
public class TqAppConfigPO extends BasePO {

    @ApiModelProperty(value = "应用名称")
    private String appName;
    @ApiModelProperty(value = "应用描述")
    private String appDesc;
    @ApiModelProperty(value = "应用申请人")
    public String appPrincipal;
    @ApiModelProperty(value = "应用账号")
    public String appAccount;
    @ApiModelProperty(value = "密码/加密")
    public String appPassword;
}
