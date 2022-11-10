package com.fisk.common.service.dbBEBuild.common.impl;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.sqlserver.visitor.SQLServerSchemaStatVisitor;
import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.common.BuildCommonHelper;
import com.fisk.common.service.dbBEBuild.common.IBuildCommonSqlCommand;
import com.fisk.common.service.dbBEBuild.common.dto.DruidFieldInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        druidAnalyseSql("SELECT  \n" +
                "    A.PUID AS TT \n" +
                "      ,A.PA9_DELTACOSTFORCHANGE \n" +
                "      ,A.PA9_DELTAPRICEFORCHANGE \n" +
                "      ,A.PA9_STATE \n" +
                "      ,A.PA9_APPLYSTATUS \n" +
                "      ,A.PA9_CHANGETYPESCENARIO \n" +
                "      ,A.PA9_READINESS \n" +
                "      ,A.RA9_DESIGNERU \n" +
                "      ,A.RA9_DESIGNERC \n" +
                "      ,A.RA9_CUSTOMERU \n" +
                "      ,A.RA9_CUSTOMERC \n" +
                "      ,A.RA9_ERRORREPORTU \n" +
                "      ,A.RA9_ERRORREPORTC \n" +
                "      ,A.RA9_ANALYSTU \n" +
                "      ,A.RA9_ANALYSTC \n" +
                "      ,A.RA9_LEADDESIGNERU \n" +
                "      ,A.RA9_LEADDESIGNERC \n" +
                "      ,A.PA9_ESTIMPDATE \n" +
                "      ,A.PA9_CUSTOMERNOTIFIED \n" +
                "      ,A.PA9_CUSTOMERAPPROVED \n" +
                "      ,A.PYF5_TOTALANNUALCOST \n" +
                "      ,A.PYF5_TARGETDATEVERIFIED \n" +
                "      ,A.RYF5_RELEASECOORDU \n" +
                "      ,A.RYF5_RELEASECOORDC \n" +
                "      ,A.PYF5_REASONFOROPEN \n" +
                "      ,A.PYF5_PURQUOTEDOCCHECKED \n" +
                "      ,A.PYF5_PURPRICINGREQDUPDATE \n" +
                "      ,A.PYF5_PURISQUOTEREQUIRED \n" +
                "      ,A.PYF5_PURCHKLISTCOMPLETED \n" +
                "      ,A.PYF5_PURCNREVIEWED \n" +
                "      ,A.RYF5_PROCUREMENTU \n" +
                "      ,A.RYF5_PROCUREMENTC \n" +
                "      ,A.RYF5_MATERIALSU \n" +
                "      ,A.RYF5_MATERIALSC \n" +
                "      ,A.PYF5_KEYIMPACTSASSOLUTIONS \n" +
                "      ,A.PYF5_ISMULTPROGRAMSIMPACT \n" +
                "      ,A.PYF5_INVESTMENTCOST \n" +
                "      ,A.RYF5_INDUSTRIALDESIGNU \n" +
                "      ,A.RYF5_INDUSTRIALDESIGNC \n" +
                "      ,A.PYF5_IMPLEMENTATIONDATE \n" +
                "      ,A.PYF5_IMPLEMENTDATEPROVIDED \n" +
                "      ,A.RYF5_ENGINEERINGMGRU \n" +
                "      ,A.RYF5_ENGINEERINGMGRC \n" +
                "      ,A.PYF5_CUSTOSMPLSBMTIMPCT \n" +
                "      ,A.PYF5_CUSTOAUTHNUMBER \n" +
                "      ,A.PYF5_COMMONENGPART \n" +
                "      ,A.RYF5_CIBU \n" +
                "      ,A.RYF5_CIBC \n" +
                "      ,A.PYF5_AFFCTSSVCPARTS \n" +
                "      ,A.PYF5_AFFCTSSFTYERGOS \n" +
                "      ,A.PYF5_SALESFINANCEIMPACTED \n" +
                "      ,A.PYF5_RELEASETECHIMPACTED \n" +
                "      ,A.PYF5_QUALITYIMPACTED \n" +
                "      ,A.PYF5_PROCUREMENTIMPACTED \n" +
                "      ,A.PYF5_OTHERIMPACTED \n" +
                "      ,A.PYF5_MATERIALSIMPACTED \n" +
                "      ,A.PYF5_MANUFACTURINGIMPACTED \n" +
                "      ,A.PYF5_LEGACYDETAILFORAUDIT \n" +
                "      ,A.PYF5_ISDATABACKEDUP \n" +
                "      ,A.PYF5_INDDESIGNIMPACTED \n" +
                "      ,A.PYF5_AFFECTEDPRGIMPACTED \n" +
                "      ,A.PYF5_CUSTOPRNUMBER \n" +
                "      ,A.PYF5_CHANGESCOPE \n" +
                "      ,A.PYF5_YFV_EWO_NO \n" +
                "      ,A.PYF5_TUNING \n" +
                "      ,A.PYF5_DRTYPE \n" +
                "      ,A.PYF5_CADIMPACTED \n" +
                "      ,A.PYF5_YFVEWONO \n" +
                "      ,A.PYF5_SUPPLIER \n" +
                "      ,A.PYF5_PRODUCTNAME \n" +
                "      ,A.PYF5_INVOLVEDSUPPLIER \n" +
                "      ,A.PYF5_IMPACTSPRODREMNTS \n" +
                "      ,A.PYF5_IMPACTSIMDS \n" +
                "      ,A.PYF5_ECDNO \n" +
                "      ,A.PYF5_DEVIATIONSTATE \n" +
                "      ,A.PYF5_CUSTOMEREWONO \n" +
                "      ,A.PYF5_CUSTOMERCHANGENO \n" +
                "      ,A.PYF5_CUSTAPPRREQUIRED \n" +
                "      ,A.PYF5_CHANGEREASON \n" +
                "      ,A.PYF5_CHANGECONTEXT \n" +
                "      ,A.RYF5_CPPPLATFORMENGU \n" +
                "      ,A.RYF5_CPPPLATFORMENGC \n" +
                "      ,A.PYF5_C3OWNINGUSERID \n" +
                "      ,A.PYF5_BOMUPDATE \n" +
                "      ,A.PYF5_AUTHMATURITY \n" +
                "      ,A.PYF5_AICHANGETYPE \n" +
                "      ,A.PYF5_YFIPMOIMPACTED \n" +
                "      ,A.PYF5_SALESIMPACTED \n" +
                "      ,A.PYF5_BUPMOIMPACTED \n" +
                "      ,A.PYF5_ITPMIMPACTED \n" +
                "      ,A.PYF5_COSTANALYSTIMPACTED \n" +
                "      ,A.PA9_READINESSWARN \n" +
                "from INFODBA.PA9_AUTOCNREVISION A\n" +
                "join  INFODBA.ppom_application_object  B on (A.PUID=B.PUID)\n" +
                "where (B.PCREATION_DATE >to_date(@start_time,'yyyy-mm-dd hh24:mi:ss') \n" +
                "       and B.PCREATION_DATE<=to_date(@end_time,'yyyy-mm-dd hh24:mi:ss'))\n" +
                "or   (B.PLAST_MOD_DATE>to_date(@start_time,'yyyy-mm-dd hh24:mi:ss')  \n" +
                "      and B.PLAST_MOD_DATE<=to_date(@end_time,'yyyy-mm-dd hh24:mi:ss'))\n" +
                "      \n" +
                "      \n" +
                "      \n");
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
                Set<String> set = new HashSet<>();
                List<SQLSelectItem> SourceBName = ((SQLSelectQueryBlock) ((SQLSelect) ((SQLSelectStatement) sqlStatement)
                        .getSelect())
                        .getQuery())
                        .getSelectList();

                for (SQLSelectItem item : SourceBName) {
                    DruidFieldInfoDTO fs = new DruidFieldInfoDTO();
                    //解析表名
                    SQLPropertyExpr itemEx = (SQLPropertyExpr) item.getExpr();
                    SQLExprTableSource TableSource = (SQLExprTableSource) itemEx.getResolvedOwnerObject();
                    fs.tableName = TableSource.getExpr().toString();
                    //是否存在别名
                    if (!StringUtils.isEmpty(item.getAlias())) {
                        fs.alias = item.getAlias();
                    }
                    fs.fieldName = itemEx.getName();
                    fs.logic = item.getExpr().toString();
                    fieldList.add(fs);
                    set.add(item.getAlias());
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

}
