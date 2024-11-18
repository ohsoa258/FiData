package com.fisk.datamanagement;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.List;

public class SqlTableExtractor {

    public static void main(String[] args) {
        String sql = "SELECT  NEWID() AS uGUID,B.uGUID AS upsWorkFlowCardGUID,B.upsWorkFlowCardGUID AS upsWorkFlowCardGUIDSource,\n" +
                " (CASE WHEN dReleaseDate IS NULL\n" +
                "\t\t\t\tTHEN '未审核' ELSE\n" +
                "         \tCASE  WHEN  EXISTS(SELECT TOP 1 1 \n" +
                "\t\t\t\t\t\t\tFROM dbo.fact_sdOrderHdrHistory\n" +
                "\t\t\t\t\t\t\tWHERE usdOrderHdrGUID=A.usdOrderHdrGUID \n" +
                "\t\t\t\t\t\t\t\t   AND tApprovedTime IS  NULL \n" +
                "\t\t\t\t\t\t\t\t   AND bUsable ='t' ORDER BY tCreateTime DESC )\n" +
                "\t\t\t\t  \tTHEN '改单未审核'\n" +
                "                  ELSE '已审核'  \n" +
                "\t\t\tEND\n" +
                "        END ) AS sAuditStatus ,d_c.sCustomerNo,a.sPatternHeight,a.sCustomerSpecification,\n" +
                "\t\td_c.sCustomerName,A.sOrderNo,A.sCustomerOrderNo,A.dReceivedDate,\n" +
                "\t\td_ot.sOrderTypeName,A.spatternNO,A.sProduceInfo,\n" +
                "\t\tA.sReceivedDept,A.sMaterialNo,A.sYarnInfo,A.sMaterialNoSample,A.sStyleNo,\n" +
                "\t\tA.sProductConstruction,A.sColorNo,A.nQty,A.sUnit,A.dDeliveryDate,B.sMaterialLot AS sMaterialLot,\n" +
                "\t\tA.sColorName,B.sCardNo,B.sRawFabricNo,CONVERT(NVARCHAR(20),NULL) AS sSourceCardNo,A.sProduceType,\n" +
                "\t\tB.nPlanOutputQty,B.sStatus,\tCase when B.sStatus in ('中止','完成') then null Else c.sWorkingProcedureName End  as sWorkingProcedureName,\n" +
                "\t\tCASE when B.sStatus in ('中止','完成') then null Else E.sWorkingProcedureName End  as sStationName,\n" +
                "\t\tCONVERT(NVARCHAR(200),null )  as sLocation,\n" +
                "\t\tCONVERT(DECIMAL,null) nProdQty,CONVERT(DECIMAL,null) nProdAQty,CONVERT(DECIMAL,null) nProdWAQty,CONVERT(DECIMAL,null) nProdAKQty,\n" +
                "\t\tCONVERT(DECIMAL,null) nProdBcQty,CONVERT(DECIMAL,null) nProdAFQty,CONVERT(DECIMAL,null) AS nProdWBCQty ,CONVERT(DECIMAL,null) nProdAtQty,CONVERT(DECIMAL,null) nProdXQty,\n" +
                "\t\tCONVERT(DECIMAL,null) nProdAddQty,CONVERT(DECIMAL,null) nSToutQty,CONVERT(DECIMAL,null) As nProdDQty,\n" +
                "\t\tCONVERT(DECIMAL,null) As nWorkOutQty,CONVERT(DECIMAL,null) As nRedoQty,CONVERT(DECIMAL,null) As nFacOutQty,IIF(B.nFactInputQty<0,0,B.nFactInputQty)As nFactOutputQty,CONVERT(NVARCHAR(50),null) sCheckStatus,\n" +
                "\t\tB.tCreateTime,CONVERT(NVARCHAR(10),B.tCreateTime,120) AS tCreateDate,\n" +
                "\t\tB.sRepairType,F.sUserName,B.sLinkType,CONVERT(DECIMAL,null) As nSampleQty,B.uppTrackJobGUIDLast,\n" +
                "\t\tpp.tFactStartTime As tFactStartTime,CONVERT(NVARCHAR(Max),null) sRemark,Convert(datetime,Null) As tSalesOutTime,Convert(datetime,Null) As tProdInTime,Convert(datetime,Null) As tBCPProdInTime,\n" +
                "\t\tConvert(datetime,Null) As tCreateJBTime,\n" +
                "\t\tPP.tFactEndTime As tTrackOutTimeLast,\n" +
                "\t\tconvert(decimal(18,1),null) as nDelayHour,\n" +
                "\t\tConvert(datetime,Null) As tReceTime, Null As sToCardNo,\n" +
                "\t\tCASE WHEN A.utmArtHdrGUID=b.utmArtHdrGUID  THEN '可同步' ELSE '未同步' END AS sSynchronization,A.sCloseStatus,A.tCloseTime,b.sProduceRemark ,b.nGrayQty,A.sPlanner,A.sCustomerNameLevel,\n" +
                "\t\tCASE WHEN pp1.tFactStartTime is null and b.sStatus in ('正常' ,'异常') Then '待生产'  When pp1.tFactStartTime is not null and b.sStatus='正常'\n" +
                "\t\t   THEN '生产中' Else Null End sProduceStatus,b.sRawGrade,Convert(datetime,Null) As tJkTime,b.sProductFactory,CONVERT(NVARCHAR(10),NULL) AS sArtNo1,\n" +
                "\t\tCONVERT(NVARCHAR(10),NULL) AS sArtNo2,CONVERT(NVARCHAR(10),NULL) AS sArtNo3,CONVERT(NVARCHAR(10),NULL) AS sArtType,CONVERT(NVARCHAR(20),null )  as  sArea,CONVERT(NVARCHAR(20),null )  as  sFlowNo,CONVERT(NVARCHAR(20),'''' ) As sFVatStatus,\n" +
                "\t\tA.sStretchType,CONVERT(DECIMAL,null) As nJKQty,Case When IsNULL(tm.iRGB,0)=0 then 16777215 else tm.iRGB end  As iRGB ,TM.sColorCatena,B.upbWorkingProcedureGUIDCurrent,CONVERT(NVARCHAR(Max),'''') As sWJStatus,CONVERT(DATETIME,null) As dCalcDeliveryDate,a.nShipRate,a.sTechDept,a.sSeaSon,convert(decimal(16,0),null) as nTrackpeibuQty,b.sBatchType,convert(decimal(18,2),null) as nStoreQty,\n" +
                "\t\tCONVERT(nvarchar(20),null) as sColorStatus\t\t  \n" +
                "FROM dbo.fact_sdOrder A WITH(NOLOCK) \n" +
                "LEFT JOIN dbo.fact_psWorkFlowCard B WITH(NOLOCK) ON A.usdOrderLotGUID=B.usdOrderLotGUID\n" +
                "LEFT JOIN dbo.fact_pbWorkingProcedure C WITH(NOLOCK)  ON B.upbWorkingProcedureGUIDCurrent=C.uGUID\n" +
                "LEFT JOIN dbo.fact_pbWorkingProcedure e WITH(NOLOCk) ON C.upbWorkingProcedureGUID=E.uGUID\n" +
                "LEFT JOIN dbo.fact_smUser f WITH(NOLOCK) ON B.sCreator=F.sUserid\n" +
                "LEFT JOIN dbo.fact_ppTrackJob pp With(NoLOCK) on pp.uGUid=B.uppTrackJobGUIDLast\n" +
                "LEFT JOIN dbo.fact_ppTrackJob pp1 With(NoLock) on pp1.uGuid=B.uppTrackJobGuidCurrent\n" +
                "LEFT JOIN dbo.fact_tmColor TM With(NoLock) ON B.utmColorGUID=tm.uGUID\n" +
                "LEFT JOIN dbo.dim_pbCustomer d_c With(NoLock) ON A.pbCustomerkey=d_c.pbCustomerkey\n" +
                "LEFT JOIN dbo.dim_sdOrderType d_ot With(NoLock) ON A.sdOrderTypekey=d_ot.sdOrderTypekey";

        try {
            Select selectStatement = (Select) CCJSqlParserUtil.parse(sql);
            TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
            List<String> tableList = tablesNamesFinder.getTableList((Statement) selectStatement);

            System.out.println("查询中的表名:");
            for (String table : tableList) {
                System.out.println(table);
            }
        } catch (JSQLParserException e) {
            System.err.println("SQL 解析失败: " + e.getMessage());
        }
    }
}
