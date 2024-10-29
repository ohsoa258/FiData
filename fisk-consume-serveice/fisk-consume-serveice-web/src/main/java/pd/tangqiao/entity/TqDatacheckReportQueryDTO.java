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
public class TqDatacheckReportQueryDTO {
    /**
     * 搜索条件
     */
    @ApiModelProperty(value = "搜索条件")
    public String keyword;
    /**
     * 分页对象
     */
    @ApiModelProperty(value = "分页对象")
    public Page<TqDatacheckReportVO> page;
}
