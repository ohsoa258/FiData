package com.fisk.datamodel.dto.atomicindicator;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class AtomicIndicatorsQueryDTO {
    /**
     * 事实表id
     */
    @ApiModelProperty(value = "事实表id")
    public long factId;
    /**
     * 分页返回对象
     */
    @ApiModelProperty(value = "分页返回对象")
    public Page<AtomicIndicatorsResultDTO> page;

}
