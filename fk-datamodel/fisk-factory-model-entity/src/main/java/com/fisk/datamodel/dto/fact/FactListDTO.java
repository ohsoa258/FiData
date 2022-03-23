package com.fisk.datamodel.dto.fact;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author JianWenYang
 */
@Data
public class FactListDTO extends FactDTO {
    /**
     * 创建时间
     */
    public LocalDateTime createTime;
}
