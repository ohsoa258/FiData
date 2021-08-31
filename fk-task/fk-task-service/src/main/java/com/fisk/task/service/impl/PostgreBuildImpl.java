package com.fisk.task.service.impl;

import com.fisk.common.entity.BusinessResult;
import com.fisk.common.enums.task.BusinessTypeEnum;
import com.fisk.task.service.IPostgreBuild;
import com.fisk.task.utils.PostgreHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PostgreBuildImpl implements IPostgreBuild {

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
    public BusinessResult postgreDataStgToOds(String stgTable,String odsTable)
    {
        boolean re = false;
        String msg = null;
        BusinessResult res = new BusinessResult(re, msg);
        return res;
    }

}
