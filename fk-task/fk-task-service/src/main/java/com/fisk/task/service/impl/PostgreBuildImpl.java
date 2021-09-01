package com.fisk.task.service.impl;

import com.fisk.common.entity.BusinessResult;
import com.fisk.common.enums.task.BusinessTypeEnum;
import com.fisk.task.service.IPostgreBuild;
import com.fisk.task.utils.PostgreHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
    public BusinessResult postgreDataStgToOds(String stgTable,String odsTable)
    {
        boolean re = false;
        String msg = null;
        int proc_res=0;
        BusinessResult res = new BusinessResult(re, msg);
        String procedure = "{ call data_stg_to_ods(?, ?)}";
        try {
            Connection conn=PostgreHelper.getConnection(DatainputUrl);
            CallableStatement statement = conn.prepareCall(procedure);
            //通过 setXXX 方法将值传给IN参数
            statement.setString(1, stgTable);
            statement.setString(2, odsTable);
            statement.execute();
            proc_res=statement.getInt(3);
            log.info(proc_res+"");
        }
        catch (Exception e){
            //捕捉错误
            log.error(e.getMessage());
            msg = e.getMessage();
        }
        res = BusinessResult.of(re, msg, proc_res);
        return res;
    }

}
