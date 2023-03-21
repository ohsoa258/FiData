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
import java.util.List;
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
        //创建封装数据来源
        List<DataSourceReportDTO> dataSourceReportDTOS = new ArrayList<>();
        DataSourceReportDTO sourceReportDTO = null;

        //获取所有应用
        List<AppRegistrationPO> appRegistrationPOS = appRegistration.query().list();
        //获取所有表
        List<TableAccessPO> tableAccessPOS = tableAccess.query().list();
        //获取所有字段
        List<TableFieldsPO> tableFieldsPOS = iTableFields.query().list();
        //一个应用下多张表的字段集合
        long fildCounts=0;
        //循环封装结果集
        for (AppRegistrationPO registrationPO : appRegistrationPOS) {
            //每次进入先清零
            fildCounts=0;
            List<TableAccessPO> collect = tableAccessPOS.stream().filter(t -> t.getAppId().equals(registrationPO.getId())).collect(Collectors.toList());
            sourceReportDTO = new DataSourceReportDTO();
            sourceReportDTO.setAppId(registrationPO.getId());
            sourceReportDTO.setAppName(registrationPO.getAppName());
            sourceReportDTO.setAccessSumData(collect.size());
            for (TableAccessPO tableAccessPO : collect) {
                long temp = tableFieldsPOS.stream().filter(f -> f.getTableAccessId().equals(tableAccessPO.getId())).count();
                fildCounts+=temp;
            }
            sourceReportDTO.setTotalaMountOfData(fildCounts);
            //计算百分比
            sourceReportDTO.setProportionOfAccess(this.computerPercentage(sourceReportDTO.getAccessSumData(),tableAccessPOS.size()));
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
    public long computerPercentage(long appTableAccessCount,long tableAccessCount){

        return appTableAccessCount/tableAccessCount*100;
    }
}
