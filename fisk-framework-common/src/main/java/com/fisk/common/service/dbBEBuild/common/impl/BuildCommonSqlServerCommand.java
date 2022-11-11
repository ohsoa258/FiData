package com.fisk.common.service.dbBEBuild.common.impl;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.sqlserver.visitor.SQLServerSchemaStatVisitor;
import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.fastjson.JSON;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.common.BuildCommonHelper;
import com.fisk.common.service.dbBEBuild.common.IBuildCommonSqlCommand;
import com.fisk.common.service.dbBEBuild.common.dto.DruidFieldInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JianWenYang
 */
@Slf4j
public class BuildCommonSqlServerCommand implements IBuildCommonSqlCommand {

    @Override
    public String buildAllDbSql() {
        return "SELECT name as dbname FROM  master..sysdatabases WHERE name NOT IN ( 'master', 'model', 'msdb', 'tempdb', 'northwind','pubs' )";
    }

    @Test
    public void test() {
        String sql = "SELECT [ID]\n" +
                "      ,[Created]\n" +
                "      ,[Modified]\n" +
                "      ,[CreatedBy]\n" +
                "      ,[ModifiedBy]\n" +
                "      ,[IsDeleted]\n" +
                "      ,[Type]\n" +
                "      ,[Title]\n" +
                "      ,[Description]\n" +
                "      ,[Summary]\n" +
                "      ,[Keywords]\n" +
                "      ,[IsOnline]\n" +
                "      ,[PublishDate]\n" +
                "      ,[Content]\n" +
                "      ,[Speaker]\n" +
                "      ,[Avatar]\n" +
                "      ,[IsComment]\n" +
                "      ,[PraiseCount]\n" +
                "      ,[ReadCount]\n" +
                "      ,[CollectionCount]\n" +
                "      ,[CommentCount]\n" +
                "      ,[Src]\n" +
                "      ,[IsScore]\n" +
                "      ,[TitleImg]\n" +
                "      ,[AverageScore]\n" +
                "      ,[OriginalID]\n" +
                "      ,[VideoID]\n" +
                "      ,[PPTUrl]\n" +
                "      ,[DoctorID]\n" +
                "      ,[OriginalDoctorID]\n" +
                "      ,[LiveBeginDate]\n" +
                "      ,[LiveEndDate]\n" +
                "      ,[LiveStatus]\n" +
                "      ,[DoctorName]\n" +
                "      ,[LiveLink]\n" +
                "      ,[VideoUrl]\n" +
                "      ,[VideoImg]\n" +
                "      ,[NameStr]\n" +
                "      ,[NameStrList]\n" +
                "      ,[MediaType]\n" +
                "      ,[Live_CHANNEL_ID]\n" +
                "      ,[Live_SECRET]\n" +
                "      ,[Live_YOUR_DOMAIN]\n" +
                "      ,[Live_VN_roomID]\n" +
                "      ,[Live_VN_companyCode]\n" +
                "      ,[Live_YL_Secret]\n" +
                "      ,[Live_TypeName]\n" +
                "      ,[OuterLink]\n" +
                "      ,[QrcodeUrl]\n" +
                "      ,[WechatQrcodeUrl]\n" +
                "      ,[ShareTitle]\n" +
                "      ,[ShareDesc]\n" +
                "      ,[ShareImg]\n" +
                "      ,[WatchLiveCount]\n" +
                "      ,[ViewRate]\n" +
                "      ,[ViewMaxCount]\n" +
                "      ,[isrecommend]\n" +
                "      ,[QrViewUrl]\n" +
                "      ,[TagStr]\n" +
                "      ,[TagStrList]\n" +
                "      ,[IsBanner]\n" +
                "      ,[BeforerField]\n" +
                "      ,[BeforeScope]\n" +
                "      ,[BeforeTag]\n" +
                "      ,[IsTransmit]\n" +
                "      ,[IsHot]\n" +
                "      ,[TransmitCount]\n" +
                "      ,[BeforeTypeBig]\n" +
                "  FROM [dmuat.chugaipharma.com.cn].[dbo].[Article]\n" +
                "  where isnull(modified,created)>@start_time and isnull(modified,created)<@end_time";
        List<DruidFieldInfoDTO> fieldInfoDTOS = druidAnalyseSql(sql);
        String test = "";

    }

    @Override
    public List<DruidFieldInfoDTO> druidAnalyseSql(String sql) {
        //加载驱动
        String dbType = JdbcConstants.SQL_SERVER.toString();
        //连接druid驱动
        List<SQLStatement> stmtList = BuildCommonHelper.connectionStatement(dbType, sql);
        try {
            //解析sql，获取表名
            for (SQLStatement sqlStatement : stmtList) {
                SQLServerSchemaStatVisitor visitor = new SQLServerSchemaStatVisitor();
                sqlStatement.accept(visitor);

                //获取字段以及别名
                List<DruidFieldInfoDTO> fieldList = new ArrayList<>();
                //Set<String> set = new HashSet<>();
                List<SQLSelectItem> SourceBName = ((SQLSelectQueryBlock) ((SQLSelect) ((SQLSelectStatement) sqlStatement)
                        .getSelect())
                        .getQuery())
                        .getSelectList();

                for (SQLSelectItem item : SourceBName) {
                    DruidFieldInfoDTO fs = new DruidFieldInfoDTO();
                    /*if(item.getExpr() instanceof SQLPropertyExpr){
                        SQLPropertyExpr itemEx = (SQLPropertyExpr) item.getExpr();
                        SQLExprTableSource TableSource = (SQLExprTableSource) itemEx.getResolvedOwnerObject();
                        fs.tableName = TableSource.getExpr().toString();
                        if (fs.tableName.indexOf(".") > 1) {
                            String[] split = fs.tableName.split(".");
                            fs.tableName = split[1];
                            fs.schema = split[0];
                        }
                        //是否存在别名
                        if (!StringUtils.isEmpty(item.getAlias())) {
                            fs.alias = item.getAlias();
                        }

                    }else {
                        fs.fieldName = item.getAlias();
                        fs.logic = item.getExpr().toString();

                    }
                    fieldList.add(fs);*/
                }
                ////System.out.println("解析结果："+JSON.toJSONString(fieldList));
                log.info("解析结果：{}", JSON.toJSONString(fieldList));
                return fieldList;
            }
        } catch (Exception e) {
            log.error("Druid解析SQL异常：{}", e);
            throw new FkException(ResultEnum.DRUID_ERROR);
        }
        return null;
    }

    @Override
    public String buildColumnInfo(String dbName, String tableName) {
        StringBuilder str = new StringBuilder();
        str.append("SELECT ");
        str.append("TABLE_NAME AS table_name,");
        str.append("CHARACTER_MAXIMUM_LENGTH AS column_length,");
        str.append("COLUMN_NAME AS column_name,");
        str.append("DATA_TYPE AS data_type,");
        str.append("TABLE_SCHEMA AS schema ");
        str.append("FROM ");
        str.append("INFORMATION_SCHEMA.COLUMNS ");
        str.append("WHERE ");
        str.append("TABLE_NAME in");
        str.append("(");
        str.append(tableName);
        str.append(")");
        return str.toString();
    }

}
