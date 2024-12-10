package com.fisk.datagovernance.dto.dataquality.datacheck;

import lombok.Data;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2024-12-04
 * @Description:
 */
@Data
public class ApiFieldEditDTO {
    private Integer apiId;
    private List<ApiFieldDTO> fieldDTOS;
}
