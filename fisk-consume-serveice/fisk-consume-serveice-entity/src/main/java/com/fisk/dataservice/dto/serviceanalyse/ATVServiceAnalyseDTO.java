package com.fisk.dataservice.dto.serviceanalyse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-22 11:22
 * @description 服务数据分析DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ATVServiceAnalyseDTO {
    private long serviceNumber; //服务总个数
    private long serviceCount; //服务总次数
}
