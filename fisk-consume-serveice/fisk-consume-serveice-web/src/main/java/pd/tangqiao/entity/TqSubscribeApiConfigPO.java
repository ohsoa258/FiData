package pd.tangqiao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author wangjian
 * @date 2024-10-29 15:00:58
 */
@TableName("tq_subscribe_api_config")
@Data
public class TqSubscribeApiConfigPO extends BasePO {

    @ApiModelProperty(value = "appId")
    private Integer appId;
    @ApiModelProperty(value = "api名称")
    private String apiName;
    @ApiModelProperty(value = "api编码")
    private String apiCode;
    @ApiModelProperty(value = "api描述")
    private String apiDesc;
}
