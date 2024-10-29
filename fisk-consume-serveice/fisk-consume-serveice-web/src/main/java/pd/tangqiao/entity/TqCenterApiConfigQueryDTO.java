package pd.tangqiao.entity;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2024-10-29
 * @Description:
 */
@Data
public class TqCenterApiConfigQueryDTO {
    @ApiModelProperty(value = "appId")
    public Integer appId;
    @ApiModelProperty(value = "搜索条件")
    public String keyword;
    @ApiModelProperty(value = "分页对象")
    public Page<TqCenterApiConfigVO> page;
}
