package com.fisk.task.dto.statistics;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.task.vo.statistics.PipelLineDetailVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-07-24
 * @Description:
 */
@Data
public class PipelLineDetailDTO {
    @ApiModelProperty("天数")
    public Integer lookday;
    @ApiModelProperty("管道名称")
    public String workflowName;
    @ApiModelProperty(value = "page")
    public Page<PipelLineDetailVO> page;
}
