package com.fisk.task.service.nifi.impl;

import com.alibaba.fastjson.JSONArray;
import com.fisk.common.core.baseObject.entity.BusinessResult;
import com.fisk.common.core.enums.task.BusinessTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.task.dto.doris.UpdateLogAndImportDataDTO;
import com.fisk.task.service.nifi.IJdbcBuild;
import com.fisk.task.utils.PostgreHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class JdbcBuildImpl implements IJdbcBuild {


    @Resource
    public PostgreHelper postgreHelper;

    @Override
    public BusinessResult postgreBuildTable(String executsql, BusinessTypeEnum businessTypeEnum) {
        boolean re = false;
        String msg = null;
        try {
            postgreHelper.postgreExecuteSql(executsql,businessTypeEnum);
            re = true;
        } catch (Exception e) {
            //捕捉错误
            log.error(e.getMessage());
            msg = e.getMessage();
            throw new FkException(ResultEnum.TASK_TABLE_CREATE_FAIL);
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
            data = postgreHelper.postgreQuery(executsql, businessTypeEnum, data);
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
             resultSet = postgreHelper.postgreQuery(executsql, businessTypeEnum);
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
