package com.fisk.datamodel.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class DataAreaPageDTO {
    public String where;
    public Page<DataAreaDTO> page;
    public long businessId;
}
