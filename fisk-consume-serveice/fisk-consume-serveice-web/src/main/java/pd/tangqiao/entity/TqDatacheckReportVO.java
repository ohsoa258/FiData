package pd.tangqiao.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Author: wangjian
 * @Date: 2024-10-29
 * @Description:
 */
@Data
public class TqDatacheckReportVO {
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
    @ApiModelProperty(value = "发生时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
