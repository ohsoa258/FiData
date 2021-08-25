package com.fisk.task.service.impl;

import com.fisk.common.entity.BusinessResult;
import com.fisk.common.enums.task.BusinessTypeEnum;
import com.fisk.task.service.IPostgreBuild;
import com.fisk.task.utils.DorisHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Objects;

@Service
@Slf4j
public class PostgreBuildImpl implements IPostgreBuild {
    @Value("${pgsql-datamodel.url}")
    private String pgsqlDatamodelUrl;
    @Value("${pgsql-datainput.driverClassName}")
    private String pgsqlDriverClassName;
    @Value("${pgsql-datainput.url}")
    private String pgsqlDatainputUrl;
    @Value("${pgsql-datainput.username}")
    private String pgsqlUsername;
    @Value("${pgsql-datainput.password}")
    private String pgsqlPassword;


    @Override
    public BusinessResult postgreBuildTable(String executsql,String dbName) {
        boolean re = false;
        String msg = null;
        String pgDatamodelUrl=pgsqlDatamodelUrl;
        String pgDriverClassName=pgsqlDriverClassName;
        String pgDatainputUrl=pgsqlDatainputUrl;
        String pgUsername=pgsqlUsername;
        String pgPassword=pgsqlPassword;
        Connection conn = null;
        Statement stmt = null;
        try {
            // 1获得连接
            if(Objects.equals(dbName, BusinessTypeEnum.DATAMODEL.getName())){
                conn = DorisHelper.getConnection(pgDatamodelUrl, pgDriverClassName, pgUsername, pgPassword);
            }else if(Objects.equals(dbName, BusinessTypeEnum.DATAINPUT.getName())){
                conn = DorisHelper.getConnection(pgDatainputUrl, pgDriverClassName, pgUsername, pgPassword);
            }
            // 2执行对象
            stmt = conn.createStatement();
            // 3执行
            stmt.executeUpdate(executsql);
            re = true;

        } catch (Exception e) {
            //捕捉错误
            log.error(e.getMessage());
            msg = e.getMessage();
        } finally {
            //关闭操作对象
            DorisHelper.closeStatement(stmt);
            //关闭连接
            DorisHelper.closeConn(conn);
        }
        BusinessResult res = new BusinessResult(re, msg);
        return res;
    }
}
