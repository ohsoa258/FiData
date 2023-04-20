package com.fisk.chartvisual.dto.chartvisual;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.chartvisual.vo.ChartPropertyVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gy
 */
@Data
public class ChartQueryDTO {

    @ApiModelProperty(value = "id")
    public Long id;

    @ApiModelProperty(value = "名称")
    public String name;

    @ApiModelProperty(value = "详细信息")
    public String details;

    @ApiModelProperty(value = "目录")
    public String content;

    @ApiModelProperty(value = "文件夹ID")
    public Long folderId;

    @ApiModelProperty(value = "页")
    public Page<ChartPropertyVO> page;
}
