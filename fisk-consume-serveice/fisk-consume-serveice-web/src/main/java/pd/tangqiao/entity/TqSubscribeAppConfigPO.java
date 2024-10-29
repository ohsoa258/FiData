package pd.tangqiao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author wangjian
 * @date 2024-10-29 15:00:58
 */
@TableName("tq_subscribe_app_config")
@Data
public class TqSubscribeAppConfigPO extends BasePO {

    @ApiModelProperty(value = "应用名称")
    private String appName;
    @ApiModelProperty(value = "应用描述")
    private String appDesc;
    @ApiModelProperty(value = "创建人")
    private String appPrincipal;
}
