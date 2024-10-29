package pd.tangqiao.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2024-10-29
 * @Description:
 */
@Data
public class TqSubscribeAppConfigVO {
    @ApiModelProperty(value = "id")
    public Integer id;
    @ApiModelProperty(value = "应用名称")
    public String appName;
    @ApiModelProperty(value = "应用描述")
    public String appDesc;
    @ApiModelProperty(value = "接口数量")
    public int apiNumber;
    @ApiModelProperty(value = "创建人")
    public String appPrincipal;
    public List<TqSubscribeApiConfigPO> apiConfigPOS;
}
