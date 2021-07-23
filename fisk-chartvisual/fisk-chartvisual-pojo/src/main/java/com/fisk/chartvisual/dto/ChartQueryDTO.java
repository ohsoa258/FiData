package com.fisk.chartvisual.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.chartvisual.vo.ChartPropertyVO;
import lombok.Data;

/**
 * @author gy
 */
@Data
public class ChartQueryDTO {
    public Long id;
    public String name;
    public String details;
    public Page<ChartPropertyVO> page;
}
