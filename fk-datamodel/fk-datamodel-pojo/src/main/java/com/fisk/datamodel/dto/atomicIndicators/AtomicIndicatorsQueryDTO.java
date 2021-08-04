package com.fisk.datamodel.dto.atomicIndicators;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class AtomicIndicatorsQueryDTO {
    /**
     * 业务过程id
     */
    public int businessProcessId;
    /**
     * 分页返回对象
     */
    public Page<AtomicIndicatorsResultDTO> page;

}
