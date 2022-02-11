package com.fisk.task.service.nifi.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fisk.common.entity.BusinessResult;
import com.fisk.common.enums.task.BusinessTypeEnum;
import com.fisk.task.dto.doris.UpdateLogAndImportDataDTO;
import com.fisk.task.service.nifi.IPostgreBuild;
import com.fisk.task.utils.PostgreHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.CallableStatement;
import java.sql.Connection;

@Service
@Slf4j
public class PostgreBuildImpl implements IPostgreBuild {


    private static String DatainputUrl;
    @Value("${pgsql-datainput.url}")
    public void setPgsqlDatainputUrl(String pgsqlDatainputUrl) {
        DatainputUrl = pgsqlDatainputUrl;
    }

    @Override
    public BusinessResult postgreBuildTable(String executsql, BusinessTypeEnum businessTypeEnum) {
        boolean re = false;
        String msg = null;
        try {
            PostgreHelper.postgreExecuteSql(executsql,businessTypeEnum);
            re = true;
        } catch (Exception e) {
            //捕捉错误
            log.error(e.getMessage());
            msg = e.getMessage();
        }
        BusinessResult res = new BusinessResult(re, msg);
        return res;
    }

    @Override
    public <T> BusinessResult postgreQuery(String executsql, BusinessTypeEnum businessTypeEnum,T data) {
        boolean re = false;
        String msg = null;
        BusinessResult res = null;
        try {
            data = PostgreHelper.postgreQuery(executsql, businessTypeEnum, data);
            re = true;
        } catch (Exception e) {
            //捕捉错误
            log.error(e.getMessage());
            msg = e.getMessage();
        }
        res = BusinessResult.of(re, msg, data);
        return res;
    }

    @Override
    public BusinessResult postgreQuery(String executsql, BusinessTypeEnum businessTypeEnum) {
        boolean re = false;
        String msg = null;
        BusinessResult res = null;
        JSONArray resultSet=null;
        try {
             resultSet = PostgreHelper.postgreQuery(executsql, businessTypeEnum);
            re = true;
        } catch (Exception e) {
            //捕捉错误
            log.error(e.getMessage());
            msg = e.getMessage();
        }
        res = BusinessResult.of(re, msg, resultSet);
        return res;
    }
    @Override
    public BusinessResult postgreDataStgToOds(String stgTable, String odsTable, UpdateLogAndImportDataDTO dto)
    {

        return null;
    }

}
