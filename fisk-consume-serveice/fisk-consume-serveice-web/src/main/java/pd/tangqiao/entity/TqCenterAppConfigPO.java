package pd.tangqiao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author wangjian
 * @date 2024-10-29 16:04:32
 */
@TableName("tq_center_app_config")
@Data
public class TqCenterAppConfigPO extends BasePO {

    @ApiModelProperty(value = "应用名称")
    private String appName;
    @ApiModelProperty(value = "应用描述")
    private String appDesc;
    @ApiModelProperty(value = "申请人")
    private String appPrincipal;
    @ApiModelProperty(value = "账号")
    private String appAccount;
    @ApiModelProperty(value = "密码/MD5加密存储")
    private String appPassword;
    @ApiModelProperty(value = "类型")
    public Integer appType;
}
