package com.fisk.dataaccess.dto.statementofassets;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-21 11:04
 * @description 数据来源报表数据展示
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataSourceReportDTO {
    private long appId; //应用ID
    private String appName; //应用名称
    private long accessSumData;// 该应用下接入表的总数
    private long totalaMountOfData; // 该应用接入表下的总条数
    private long proportionOfAccess; //该应用的接入占比
}
