package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.fisk.dataaccess.dto.statementofassets.DataSourceReportDTO;
import com.fisk.dataaccess.entity.AppRegistrationPO;
import com.fisk.dataaccess.entity.TableAccessPO;
import com.fisk.dataaccess.entity.TableFieldsPO;
import com.fisk.dataaccess.service.IAppRegistration;
import com.fisk.dataaccess.service.IDataSourceReport;
import com.fisk.dataaccess.service.ITableAccess;
import com.fisk.dataaccess.service.ITableFields;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-21 11:27
 * @description
 */
@Service
public class DataSourceReportImpl implements IDataSourceReport {
    @Resource
    IAppRegistration appRegistration;
    @Resource
    ITableAccess tableAccess;

    @Resource
    ITableFields iTableFields;

    @Override
    public List<DataSourceReportDTO> getDataSourceReportDTO() {
        List<DataSourceReportDTO> dataSourceReportDTOS = new ArrayList<>();

        // 获取所有应用
        List<AppRegistrationPO> appRegistrations = appRegistration.query().list();
        // 获取所有表
        List<TableAccessPO> tableAccesses = tableAccess.query().list();
        // 获取所有字段
        List<TableFieldsPO> tableFields = iTableFields.query().list();

        // 创建应用ID到表的映射
        Map<String, List<TableAccessPO>> appToTablesMap = tableAccesses.stream()
                .collect(Collectors.groupingBy(i->i.getAppId().toString()));

        // 创建表ID到字段的映射
        Map<String, List<TableFieldsPO>> tableToFieldsMap = tableFields.stream()
                .collect(Collectors.groupingBy(i->i.getTableAccessId().toString()));

        // 循环封装结果集
        for (AppRegistrationPO app : appRegistrations) {
            List<TableAccessPO> tablesForApp = appToTablesMap.getOrDefault(String.valueOf(app.getId()), Collections.emptyList());
            long fieldCount = tablesForApp.stream()
                    .mapToLong(table -> tableToFieldsMap.getOrDefault(String.valueOf(table.getId()), Collections.emptyList()).size())
                    .sum();

            DataSourceReportDTO sourceReportDTO = new DataSourceReportDTO();
            sourceReportDTO.setAppId(app.getId());
            sourceReportDTO.setAppName(app.getAppName());
            sourceReportDTO.setAccessSumData(tablesForApp.size());
            sourceReportDTO.setTotalaMountOfData(fieldCount);
            sourceReportDTO.setProportionOfAccess(computerPercentage(sourceReportDTO.getAccessSumData(), tableAccesses.size()));

            dataSourceReportDTOS.add(sourceReportDTO);
        }

        return dataSourceReportDTOS;
    }

    /**
     * 计算每个应用的百分比
     * @param appTableAccessCount 当前应用下接入的表总数
     * @param tableAccessCount  总接入表
     * @return
     */
    public double computerPercentage(double appTableAccessCount,double tableAccessCount){
        return (appTableAccessCount/tableAccessCount)*100d;
    }
}
