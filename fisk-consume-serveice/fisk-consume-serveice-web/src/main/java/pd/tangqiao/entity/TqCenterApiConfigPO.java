package pd.tangqiao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

/**
 * @author wangjian
 * @date 2024-10-29 16:04:32
 */
@TableName("tq_center_api_config")
@Data
public class TqCenterApiConfigPO extends BasePO {

    @ApiModelProperty(value = "api名称")
    private String apiName;
    @ApiModelProperty(value = "api编码")
    private String apiCode;
    @ApiModelProperty(value = "api描述")
    private String apiDesc;
    @ApiModelProperty(value = "状态")
    private Integer state;
    @ApiModelProperty(value = "api类型")
    private Integer apiType;
}
