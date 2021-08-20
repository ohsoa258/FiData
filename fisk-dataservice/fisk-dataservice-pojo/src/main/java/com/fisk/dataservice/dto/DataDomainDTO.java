package com.fisk.dataservice.dto;

import lombok.Data;

import java.util.List;

/**
 * @author Lock
 */
@Data
public class DataDomainDTO {

    /**
     * 筛选器 列  值
     */
    List<DataDoFieldDTO> dataDoFieldList;

}
