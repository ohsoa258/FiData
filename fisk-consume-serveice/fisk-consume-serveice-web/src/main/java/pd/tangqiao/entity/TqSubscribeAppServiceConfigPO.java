package pd.tangqiao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

/**
 * @author wangjian
 * @date 2024-10-30 15:38:27
 */
@TableName("tq_subscribe_app_service_config")
@Data
public class TqSubscribeAppServiceConfigPO extends BasePO {

    @ApiModelProperty(value = "应用id")
    private Integer appId;
    @ApiModelProperty(value = "服务id")
    private Integer serviceId;
}
