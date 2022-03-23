package com.fisk.system.dto;

import lombok.Data;

/**
 * @author WangYan
 * @date 2021/11/4 15:03
 */
@Data
public class DataViewFilterDTO {

    private Integer id;
    private Integer dataviewId;
    private String field;
    private String operator;
    private String result;
}
