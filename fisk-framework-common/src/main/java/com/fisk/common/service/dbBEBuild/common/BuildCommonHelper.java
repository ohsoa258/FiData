package com.fisk.common.service.dbBEBuild.common;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.common.impl.BuildCommonMySqlCommand;
import com.fisk.common.service.dbBEBuild.common.impl.BuildCommonOracleCommand;
import com.fisk.common.service.dbBEBuild.common.impl.BuildCommonPgSqlCommand;
import com.fisk.common.service.dbBEBuild.common.impl.BuildCommonSqlServerCommand;
import jdk.nashorn.internal.runtime.ParserException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author JianWenYang
 */
@Slf4j
public class BuildCommonHelper {

    public static IBuildCommonSqlCommand getCommand(DataSourceTypeEnum dataSourceTypeEnum) {
        switch (dataSourceTypeEnum) {
            case MYSQL:
                return new BuildCommonMySqlCommand();
            case POSTGRESQL:
                return new BuildCommonPgSqlCommand();
            case SQLSERVER:
                return new BuildCommonSqlServerCommand();
            case ORACLE:
                return new BuildCommonOracleCommand();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    /**
     * 连接druid驱动
     *
     * @param dbType
     * @param sql
     * @return
     */
    public static List<SQLStatement> connectionStatement(String dbType, String sql) {
        //格式化输出
        String sqlResult = SQLUtils.format(sql, dbType);
        System.out.println("格式化后的sql:" + sqlResult);
        List<SQLStatement> stmtList = null;
        try {
            stmtList = SQLUtils.parseStatements(sql, dbType);
        } catch (ParserException e) {
            log.info("Druid解析sql语法有误，请检查sql:{}", e);
            throw new FkException(ResultEnum.DRUID_SQL_ERROR);
        }
        return stmtList;
    }

}
