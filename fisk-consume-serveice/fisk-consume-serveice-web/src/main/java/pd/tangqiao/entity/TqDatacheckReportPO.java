package pd.tangqiao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;
/**
 * @author wangjian
 * @date 2024-10-29 10:56:49
 */
@TableName("tq_datacheck_report")
@Data
public class TqDatacheckReportPO extends BasePO {

    @ApiModelProperty(value = "状态")
    private String state;
    @ApiModelProperty(value = "场景")
    private String scenario;
    @ApiModelProperty(value = "数据项")
    private String dataItem;
    @ApiModelProperty(value = "规则")
    private String rule;
    @ApiModelProperty(value = "规则内容")
    private String ruleIllustrate;
}
