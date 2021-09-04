package com.fisk.datamodel.dto.atomicindicator;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class AtomicIndicatorFactDTO {
    public String factTable;
    public List<AtomicIndicatorPushDTO> list;
}
