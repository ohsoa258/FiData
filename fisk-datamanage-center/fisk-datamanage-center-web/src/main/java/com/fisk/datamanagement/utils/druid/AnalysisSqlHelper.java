package com.fisk.datamanagement.utils.druid;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.sqlserver.visitor.SQLServerSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.fisk.datamanagement.dto.druid.FieldStructureDTO;
import jdk.nashorn.internal.runtime.ParserException;
import org.junit.Test;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author JianWenYang
 */
@Component
public class AnalysisSqlHelper {

    @Test
    public void test() {
        analysisTableSql("SELECT  DISTINCT\n" +
                "    'NIMS'+ CONVERT(VarChar(50),FactIms.SID) AS ROWID\n" +
                "\t,FactIms.IMSID\n" +
                "    ,ISNULL(CONVERT(INT,FactIms.DateID),-1) AS DateID\n" +
                "    ,FactIms.SalesDate\n" +
                "    ,FactIms.BU\n" +
                "    ,ISNULL(DimSFDCProductLine.ProductLine,'Others') AS ProductLine\n" +
                "\n" +
                "    ,ISNULL(FactIms.DistID,-1) AS DistID\n" +
                "    ,ISNULL(FactIms.CustID,-1) AS CustID\n" +
                "  ,CAST('-1' AS INT) AS AXCustID\n" +
                "    ,SUBSTRING(FactIms.Flow,1,CHARINDEX('->',FactIms.Flow)-1) AS SellerTier\n" +
                "    ,SUBSTRING(FactIms.Flow,CHARINDEX('->',FactIms.Flow)+2,LEN(FactIms.Flow)) AS BuyTier\n" +
                "    ,CASE WHEN DF.LevelFlow IS NULL THEN FactIms.Flow ELSE DF.LevelFlow END AS Flow\n" +
                "    ,CASE WHEN DF.LevelFlow IS NULL THEN SUBSTRING(FactIms.Flow,1,CHARINDEX('->',FactIms.Flow)-1) \n" +
                "        ELSE LEFT(DF.LevelFlow,2) END AS SellerLevel\n" +
                "    ,CASE WHEN DF.LevelFlow IS NULL THEN SUBSTRING(FactIms.Flow,CHARINDEX('->',FactIms.Flow)+2,LEN(FactIms.Flow)) \n" +
                "        ELSE RIGHT(DF.LevelFlow,2) END AS BuyerLevel\n" +
                "\n" +
                "    ,FactIms.BasicUnitPackage\n" +
                "    ,FactIms.PerfVAL*isnull(convert(DECIMAL(12,2),p.weight),1) PerfVAL\n" +
                "    ,CONVERT(DECIMAL(12,2),FactIms.QTY)*isnull(convert(DECIMAL(12,2),p.weight),1)\n" +
                "            /CASE WHEN ISNULL(CONVERT(DECIMAL(12,2),FactIms.UOM),0)=0 THEN 1 \n" +
                "                ELSE CONVERT(DECIMAL(12,2),FactIms.UOM) END AS PurchaseQty \n" +
                "    ,FactIms.QTY*isnull(convert(DECIMAL(12,2),p.weight),1) AS SellingQty\n" +
                "    ,ISNULL(CASE WHEN FactIms.QTY = 0 THEN 0 ELSE FactIms.PerfVAL/FactIms.QTY END,0) as Price\n" +
                "    ,FactIms.UOM AS UnitConversion\n" +
                "    ,FactIms.ListPrice AS ProductASP\n" +
                "    ,ISNULL(FactIms.PerfVAL,0)*isnull(convert(DECIMAL(12,2),p.weight),1) AS ActualSales\n" +
                "    ,CASE WHEN (LCP.TCS_Lite_Price=0.0 OR LCP.Pruchase_UOM=0 ) THEN 0\n" +
                "            ELSE (CONVERT(DECIMAL(12,2),FactIms.QTY)/ LCP.Pruchase_UOM)*isnull(convert(DECIMAL(12,2),p.weight),1)\n" +
                "                    * LCP.TCS_Lite_Price END AS OriginalASPSalesRevenue\n" +
                "\n" +
                "    ,ISNULL(DimProductMaster.DerivedProductID,-1) AS ProductCodeID\n" +
                "    ,CASE WHEN DimProductMaster.Main_Component=N'YES' THEN ISNULL(FactIMS.QTY,0)*isnull(convert(DECIMAL(12,2),p.weight),1)\n" +
                "\t      ELSE 0.0 END AS ProcedureScore -- OptNum\n" +
                "  \n" +
                "  ,ISNULL([VAT%],1) AS VAT\n" +
                "  ,ISNULL(FactIms.Distributor_Price_Revenue_c,0)*isnull(convert(DECIMAL(12,2),p.weight),1) AS OringalSales\n" +
                "  ,ISNULL(FactIms.Distributor_Price_Revenue_c,0)/ISNULL([VAT%],1)*isnull(convert(DECIMAL(12,2),p.weight),1)  AS TSAPriceRev\n" +
                "  ,CONVERT(DECIMAL(20,4),ISNULL(FactIms.Distributor_Price_c*FactIms.UOM,0)) AS TSAPrice\n" +
                "  ,CONVERT(DECIMAL(12,2),ISNULL(ListPrice,0)) AS ASPPrice  --ISNULL(ListPrice*FactIms.UOM,0)\n" +
                "  ,CONVERT(DECIMAL(20,4),ISNULL(CONVERT(DECIMAL(12,2),FactIms.Performance_Price_Smallest_Package_c)*CONVERT(DECIMAL(12,2),FactIms.UOM)*isnull(convert(DECIMAL(12,2),p.weight),1),0)) AS PerformancePrice\n" +
                "\n" +
                "    ,ISNULL(P.DSMAlias,'') AS DSMID\n" +
                "    ,ISNULL(P.DSM,'') DSM\n" +
                "\t,ISNULL(P.DSMTHId,'') AS DSMTH\n" +
                "    ,ISNULL(P.RSMAlias,'') AS RSMID\n" +
                "    ,ISNULL(P.RSM,'') RSM\n" +
                "\t,ISNULL(P.RSMTHId,'') AS RSMTH\n" +
                "    ,ISNULL(P.RSDAlias,'') AS RSDID\n" +
                "    ,ISNULL(P.RSD,'') RSD\n" +
                "\t,ISNULL(P.RSDTHId,'') AS RSDTH\n" +
                "    ,ISNULL(P.alias,'') AS SalesRepID\n" +
                "    ,ISNULL(P.Rep,'') AS SalesRep\n" +
                "\t,ISNULL(P.RepTHId,'') AS SalesRepTH\n" +
                "  ,ISNULL(ValidationResult,'') AS ValidationResult\n" +
                "  ,ISNULL(ValidationType,'') AS ValidationType\n" +
                "  ,ISNULL(AP2015.ASPPrice,0.00) * CONVERT(DECIMAL(12,2),FactIms.QTY)*isnull(convert(DECIMAL(12,2),p.weight),1)\n" +
                "            /CASE WHEN ISNULL(CONVERT(DECIMAL(12,2),FactIms.UOM),0)=0 THEN 1 \n" +
                "                ELSE CONVERT(DECIMAL(12,2),FactIms.UOM) END AS ASPSalesRev2015\n" +
                "  ,ISNULL(AP2016.ASPPrice,0.00) * CONVERT(DECIMAL(12,2),FactIms.QTY)*isnull(convert(DECIMAL(12,2),p.weight),1)\n" +
                "            /CASE WHEN ISNULL(CONVERT(DECIMAL(12,2),FactIms.UOM),0)=0 THEN 1 \n" +
                "                ELSE CONVERT(DECIMAL(12,2),FactIms.UOM) END AS ASPSalesRev2016\n" +
                "  ,ISNULL(AP2017.ASPPrice,0.00) * CONVERT(DECIMAL(12,2),FactIms.QTY)*isnull(convert(DECIMAL(12,2),p.weight),1)\n" +
                "            /CASE WHEN ISNULL(CONVERT(DECIMAL(12,2),FactIms.UOM),0)=0 THEN 1 \n" +
                "                ELSE CONVERT(DECIMAL(12,2),FactIms.UOM) END AS ASPSalesRev2017\n" +
                "  ,ISNULL(AP2018.ASPPrice,0.00) * CONVERT(DECIMAL(12,2),FactIms.QTY)*isnull(convert(DECIMAL(12,2),p.weight),1)\n" +
                "            /CASE WHEN ISNULL(CONVERT(DECIMAL(12,2),AP2018.UOM),0)=0 THEN 1 \n" +
                "                ELSE CONVERT(DECIMAL(12,2),AP2018.UOM) END AS ASPSalesRev2018\n" +
                "  ,CASE WHEN (LCP.TCS_Lite_Price=0.0 OR LCP.Pruchase_UOM=0 ) THEN 0\n" +
                "            ELSE (CONVERT(DECIMAL(12,2),FactIms.QTY)/ LCP.Pruchase_UOM)*isnull(convert(DECIMAL(12,2),p.weight),1)\n" +
                "                    * LCP.TCS_Lite_Price END AS ASPSalesRevenue\n" +
                "  ,FactIms.TSA_Status_ID\n" +
                "  ,isnull(FactIms.Product_Line_SFDC,N'other') as Product_Line_SFDC\n" +
                "  ,CASE WHEN ValidationType='O' THEN 'O' ELSE ISNULL([ValidationSubType],'') END AS [ValidationSubType]\n" +
                "  ,[Incentive_ID]\n" +
                "  ,factims.ResultRemark\n" +
                "  ,factims.CreateSource\n" +
                "  ,FactIms.SupportFileType\n" +
                "  ,FactIms.IsProvideInvoices\n" +
                "  ,FactIms.IMSInvoiceResult\n" +
                "  ,factims.IMSName\n" +
                "  ,ISNULL(CSF.CrossSellingFlag,N'N') as CrossSellingFlag\n" +
                "  ,ISNULL(CSF.SellingBU,N'') as SellingBU\n" +
                "    -- ,FactIms.ProductCode2,\n" +
                "    -- ,PTC.TargetCategory,\n" +
                "    -- ,D.AccountNumber,\n" +
                "    -- ,p.alias,\n" +
                "    --,isnull(convert(DECIMAL(12,2),p.weight),1)\n" +
                "    FROM dbo.FactIms WITH (NOLOCK)\n" +
                "    LEFT JOIN DimProductMaster WITH (NOLOCK) ON CONVERT(nvarchar(40),DimProductMaster.ProductCode) = CONVERT(nvarchar(40),FactIms.ProductCode2)\n" +
                "    LEFT JOIN Staging.dbo.STG_ProductTargetCategory PTC ON PTC.Year = FactIms.DateID/10000 AND PTC.item_number =FactIms.ProductCode2\n" +
                "    LEFT JOIN DataWarehouse.dbo.DimDistributor D on FactIms.CustID =D.DistID\n" +
                "    LEFT JOIN ( SELECT * FROM DataWarehouse.dbo.PositionSales WHERE Cycle_Code>=@CutOffDate/100\n" +
                "\t          ) P on left(FactIms.DateID,6)=P.Cycle_Code and PTC.TargetCategory=P.Target_Category_Code and D.AccountNumber=P.account_code\n" +
                "    LEFT JOIN DimSFDCProduct WITH (NOLOCK) ON CONVERT(nvarchar(40),FactIMS.ProductCode2) = CONVERT(nvarchar(40),DimSFDCProduct.productCode)\n" +
                "                        AND CASE WHEN FactIMS.BU = N'AWM' THEN LEFT(FactIms.DateID,4) \n" +
                "                                ELSE -1 END = DimSFDCProduct.Year AND DimSFDCProduct.IsSf = 1\n" +
                "    LEFT JOIN DimSFDCProductLine WITH (NOLOCK) ON CASE WHEN FactIms.BU = N'Trauma' THEN 'Trauma' \n" +
                "                                        ELSE DimSFDCProduct.ProductLine END = DimSFDCProductLine.ProductLine    \n" +
                "    LEFT JOIN Staging.dbo.DimFlow AS DF WITH (NOLOCK) ON DF.IMSTierFlow=FactIms.Flow\n" +
                "  LEFT JOIN Staging.dbo.BUVat AS BV WITH (NOLOCK) ON FactIms.BU = BV.BU \n" +
                "    AND CASE WHEN BV.ProductLine = 'ALL' THEN 'ALL' ELSE ISNULL(DimSFDCProductLine.ProductLine,'Others') END = BV.ProductLine\n" +
                "    AND CASE WHEN BV.SellerTier = 'ALL' THEN 'ALL' ELSE SUBSTRING(FactIms.Flow,1,CHARINDEX('->',FactIms.Flow)-1) END = BV.SellerTier\n" +
                "    AND CASE WHEN BV.BuyerTier = 'ALL' THEN 'ALL' ELSE SUBSTRING(FactIms.Flow,CHARINDEX('->',FactIms.Flow)+2,LEN(FactIms.Flow)) END = BV.BuyerTier\n" +
                "  LEFT JOIN Staging.dbo.DimRegionalBU AS RB WITH (NOLOCK) ON RB.ProductCode = FactIms.ProductCode2\n" +
                "  LEFT JOIN SFDCASPPriceHis AP2015 WITH (NOLOCK) ON DimProductMaster.DerivedProductID = AP2015.ProductCodeID AND AP2015.PriceYear=2018 AND DimProductMaster.CompanyCode = 100\n" +
                "  LEFT JOIN SFDCASPPriceHis AP2016 WITH (NOLOCK) ON DimProductMaster.DerivedProductID = AP2016.ProductCodeID AND AP2016.PriceYear=2019 AND DimProductMaster.CompanyCode = 100\n" +
                "  LEFT JOIN SFDCASPPriceHis AP2017 WITH (NOLOCK) ON DimProductMaster.DerivedProductID = AP2017.ProductCodeID AND AP2017.PriceYear=2020 AND DimProductMaster.CompanyCode = 100\n" +
                "  LEFT JOIN SFDCASPPriceHis AP2018 WITH (NOLOCK) ON DimProductMaster.DerivedProductID = AP2018.ProductCodeID AND AP2018.PriceYear=2021 AND DimProductMaster.CompanyCode = 100\n" +
                "  LEFT JOIN ( SELECT PDP.TCS_Lite_Product_Code2_c\n" +
                "                    ,CAST(ISNULL(TCS_Lite_Price_c,0.00) AS DECIMAL(12,2) ) AS TCS_Lite_Price\n" +
                "\t\t\t\t\t,CAST(CAST(ISNULL(Pruchase_Pacakage_UOM_c,0.00) AS FLOAT ) AS DECIMAL(12,2) ) AS Pruchase_UOM\n" +
                "\t\t\t\tFROM Staging.dbo.TCSLiteCompanyProduct PDP WITH (NOLOCK)\n" +
                "\t\t\t\tWHERE CurrencyIsoCode = N'CNY' AND IsDeleted = '0') LCP ON DimProductMaster.ProductCode = LCP.TCS_Lite_Product_Code2_c\t\t\t \n" +
                "  LEFT JOIN #Temp_CrossSellingFlag CSF on factims.CustID = CSF.HospitalID and factims.DistID = CSF.DistributorID and FactIms.ProductCode2 = CSF.ProductCode\n" +
                "  WHERE FactIms.IsDeleted = '0' ", "");
    }

    public List<String> analysisTableSql(String sql, String dbType) {
        List<String> tableNameList = new ArrayList<>();
        try {
            //格式化输出
            String sqlResult = SQLUtils.format(sql, dbType);
            System.out.println("格式化后的sql:" + sqlResult);
            List<SQLStatement> stmtList;
            try {
                stmtList = SQLUtils.parseStatements(sql, dbType);
            } catch (ParserException e) {
                System.out.println("sql语法有误，请检查sql");
                return tableNameList;
            }
            for (SQLStatement sqlStatement : stmtList) {
                SQLServerSchemaStatVisitor visitor=new SQLServerSchemaStatVisitor();
                sqlStatement.accept(visitor);
                //获取表名
                Map<TableStat.Name, TableStat> tables = visitor.getTables();
                System.out.println("druid解析sql的结果集:"+tables+"");
                Set<TableStat.Name> tableNameSet = tables.keySet();
                for (TableStat.Name name : tableNameSet) {
                    String tableName = name.getName();
                    if (tableName != null && tableName.length() > 0 && tableName.trim().length() > 0) {
                        tableNameList.add(tableName);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return tableNameList;
    }

    public List<FieldStructureDTO> analysisColumnSql(String sql, String dbType)
    {
        List<FieldStructureDTO> columnList = new ArrayList<>();
        try {
            //格式化输出
            String sqlResult = SQLUtils.format(sql, dbType);
            System.out.println("格式化后的sql:"+sqlResult);
            List<SQLStatement> stmtList = null;
            try {
                stmtList = SQLUtils.parseStatements(sql, dbType);
            } catch (ParserException e) {
                System.out.println("sql语法有误，请检查sql");
                return columnList;
            }
            for (SQLStatement sqlStatement : stmtList) {
                SQLServerSchemaStatVisitor visitor=new SQLServerSchemaStatVisitor();
                sqlStatement.accept(visitor);
                //获取where条件
                List<TableStat.Condition> conditions = visitor.getConditions();
                System.out.println("解析sql后的查询条件："+conditions+"");
                //别名  *详细字段 2022年01月05日18:06:30 Dennyhui
                Set<String> set = new HashSet<>();
                List<FieldStructureDTO> fields=new ArrayList<>();
                List<SQLSelectItem> sourceName = ((SQLSelectQueryBlock)((SQLSelect)((SQLSelectStatement)sqlStatement).getSelect()).getQuery()).getSelectList();
                for(SQLSelectItem item : sourceName){
                    if(item.getExpr() instanceof SQLPropertyExpr) {
                        SQLPropertyExpr itemex = (SQLPropertyExpr) item.getExpr();
                        SQLExprTableSource tableSource=(SQLExprTableSource)itemex.getResolvedOwnerObject();
                        System.out.println(tableSource.getExpr()+"."+itemex.getName());
                        FieldStructureDTO fs=new FieldStructureDTO();
                        fs.fieldName=itemex.getName();
                        fs.source=tableSource.getExpr().toString();
                        fs.logic=item.getExpr().toString();
                        fs.alias=false;
                        fields.add(fs);
                    }else {
                        FieldStructureDTO fs=new FieldStructureDTO();
                        System.out.println(item.getAlias()+","+item.getExpr());
                        fs.fieldName=item.getAlias();
                        fs.alias=true;
                        fs.logic=item.getExpr().toString();
                        fields.add(fs);
                    }
                    set.add(item.getAlias());
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return columnList;
    }

}
