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
    public long businessProcessId;
    /**
     * 事实表id
     */
    public long factId;
    /**
     * 分页返回对象
     */
    public Page<AtomicIndicatorsResultDTO> page;

}
