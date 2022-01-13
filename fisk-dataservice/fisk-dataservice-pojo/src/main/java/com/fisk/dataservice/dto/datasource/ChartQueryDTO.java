package com.fisk.dataservice.dto.datasource;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.dataservice.vo.datasource.ChartPropertyVO;
import lombok.Data;

/**
 * @author dick
 * @version v1.0
 * @description
 * @date 2022/1/6 14:51
 */
@Data
public class ChartQueryDTO {
    public Long id;
    public String name;
    public String details;
    public String content;
    public Long folderId;
    public Page<ChartPropertyVO> page;
}
