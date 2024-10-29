package pd.tangqiao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

/**
 * @author wangjian
 * @date 2024-10-29 13:46:14
 */
@TableName("tq_api_config")
@Data
public class TqApiConfigPO extends BasePO {
    @ApiModelProperty(value = "appId")
    private Integer appId;
    @ApiModelProperty(value = "数据项")
    private String dataItem;
    @ApiModelProperty(value = "字段")
    private String field;
    @ApiModelProperty(value = "操作类型")
    private String operationType;
}
