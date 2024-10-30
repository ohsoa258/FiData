package pd.tangqiao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

/**
 * @author wangjian
 * @date 2024-10-30 10:24:30
 */
@TableName("tq_app_service_config")
@Data
public class TqAppServiceConfigPO extends BasePO {

    @ApiModelProperty(value = "应用id")
    private Integer appId;
    @ApiModelProperty(value = "服务id")
    private Integer serviceId;
}
