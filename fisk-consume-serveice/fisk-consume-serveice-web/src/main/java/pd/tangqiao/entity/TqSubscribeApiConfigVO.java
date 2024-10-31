package pd.tangqiao.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2024-10-30
 * @Description:
 */
@Data
public class TqSubscribeApiConfigVO {
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "appId")
    private Integer appId;
    @ApiModelProperty(value = "api名称")
    private String apiName;
    @ApiModelProperty(value = "api描述")
    private String apiDesc;
}
