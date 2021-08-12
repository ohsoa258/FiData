package com.fisk.datamodel.dto.derivedindicator;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DerivedIndicatorsQueryDTO {

    public long factId;

    /**
     * 返回类型
     */
   public Page<DerivedIndicatorsListDTO> dto;

}
