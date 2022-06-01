package com.fisk.common.service.mdmBEBuild.impl;

import com.fisk.common.service.mdmBEBuild.CommonMethods;
import com.fisk.common.service.mdmBEBuild.IBuildSqlCommand;
import com.fisk.common.service.mdmBEBuild.dto.ImportDataPageDTO;
import com.fisk.common.service.mdmBEBuild.dto.InsertImportDataDTO;
import com.fisk.common.service.mdmBEBuild.dto.MasterDataPageDTO;
import com.fisk.common.service.pageFilter.dto.FilterQueryDTO;
import com.fisk.common.service.pageFilter.dto.OperatorVO;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * @author WangYan
 * @date 2022/4/13 18:05
 */
public class BuildSqlServerCommandImpl implements IBuildSqlCommand {

    @Override
    public String buildAttributeLogTable(String tableName) {
        StringBuilder str = new StringBuilder();
        str.append("CREATE TABLE dbo.").append(tableName).append("(");
        str.append("id INT NOT NULL,");
        str.append("model_id INT NULL,");
        str.append("entity_id INT NULL,");
        str.append("attribute_id INT NULL,");
        str.append("member_id INT NULL,");
        str.append("batch_id INT NULL,");
        str.append("version_id INT NULL,");
        str.append("old_code nvarchar ( 200 ) NULL,");
        str.append("old_value nvarchar ( 200 ) NULL,");
        str.append("new_code nvarchar ( 200 ) NULL,");
        str.append("new_value nvarchar ( 200 ) NULL ");
        str.append(");");
        return str.toString();
    }

    @Override
    public String buildInsertImportData(InsertImportDataDTO dto) {
        Date date = new Date();
        StringBuilder str = new StringBuilder();
        str.append("insert into " + dto.getTableName());
        str.append("(" + CommonMethods.getColumnNameAndValue(dto.getMembers().get(0), 0));
        str.append(",fidata_import_type,fidata_batch_code,fidata_version_id,");
        str.append("fidata_create_time,fidata_create_user,fidata_update_time,fidata_update_user,fidata_del_flag");
        str.append(")");
        str.append(" values(" + CommonMethods.getColumnNameAndValue(dto.getMembers().get(0), 1) + ","
                + dto.getImportType() + ",'" + dto.getBatchCode() + "'," + dto.getVersionId() + ",'");
        str.append(CommonMethods.getFormatDate(date) + "'," + dto.getUserId() + ",'");
        str.append(CommonMethods.getFormatDate(date) + "'," + dto.getUserId() + ",1" + ")");
        if (dto.getMembers().size() > 1) {
            for (int i = 1; i < dto.getMembers().size(); i++) {
                str.append(",(" + CommonMethods.getColumnNameAndValue(dto.getMembers().get(i), 1) + ","
                        + dto.getImportType() + ",'" + dto.getBatchCode() + "',"
                        + dto.getVersionId() + ",'");
                str.append(CommonMethods.getFormatDate(date) + "'," + dto.getUserId() + ",'");
                str.append(CommonMethods.getFormatDate(date) + "'," + dto.getUserId() + ",1" + ")");
            }
        }
        return str.toString();
    }

    @Override
    public String buildImportDataPage(ImportDataPageDTO dto) {
        return "";
    }

    @Override
    public String buildUpdateImportData(Map<String, Object> jsonObject, String tableName) {
        StringBuilder str = new StringBuilder();
        str.append("update " + tableName);
        str.append(" set fidata_import_type=" + jsonObject.get("fidata_import_type"));
        Iterator iter = jsonObject.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            if (entry.getValue() == null || entry.getValue() == "") {
                str.append("," + entry.getKey().toString() + "=null");
                continue;
            }
            str.append("," + entry.getKey().toString() + "='" + entry.getValue().toString() + "'");
        }
        str.append(" where fidata_id=" + jsonObject.get("fidata_id"));
        return str.toString();
    }

    @Override
    public String buildMasterDataPage(MasterDataPageDTO dto) {
        return "";
    }

    @Override
    public String buildVerifyRepeatCode(String tableName, String batchCode) {
        StringBuilder str = new StringBuilder();
        str.append("update " + tableName);
        str.append(" set fidata_status=3,fidata_error_msg='" + "编码重复" + "'");
        str.append(" where fidata_batch_code='" + batchCode + "' and code in");
        str.append("(select code from " + tableName);
        str.append(" where fidata_batch_code='" + batchCode + "'");
        str.append("and code <>'' and code is not null GROUP BY code HAVING count(*)>1)");
        return str.toString();
    }

    @Override
    public String buildQueryOneColumn(String tableName, String selectColumnName) {
        return "select distinct " + selectColumnName + " as columnName from " + tableName;
    }

    @Override
    public String buildQueryCount(String tableName, String queryConditions) {
        StringBuilder str = new StringBuilder();
        str.append("SELECT COUNT(*) AS totalNum FROM " + tableName);
        if (!StringUtils.isEmpty(queryConditions)) {
            str.append(" WHERE 1=1 " + queryConditions);
        }
        return str.toString();
    }

    @Override
    public String buildExportDataCount(String tableName, String queryConditions) {
        StringBuilder str = new StringBuilder();
        str.append("select count(*) as totalNum");
        str.append(",sum( case fidata_status when 1 then 1 else 0 end) as submitSuccessCount");
        str.append(",sum( case fidata_status when 2 then 1 else 0 end) as submitErrorCount");
        str.append(",sum( case fidata_status when 0 then 1 else 0 end) as successCount");
        str.append(",sum( case fidata_status when 3 then 1 else 0 end) as errorCount");
        str.append(",sum( case fidata_syncy_type when 1 then 1 else 0 end) as updateCount");
        str.append(",sum( case fidata_syncy_type when 2 then 1 else 0 end) as addCount");
        str.append(" from " + tableName);
        if (!StringUtils.isEmpty(queryConditions)) {
            str.append(" where 1=1 " + queryConditions);
        }
        return str.toString();
    }

    @Override
    public String buildQueryOneData(String tableName, String queryConditions) {
        StringBuilder str = new StringBuilder();
        str.append(" select *");
        str.append(" from " + tableName);
        str.append(" where " + queryConditions);
        return str.toString();
    }

    @Override
    public String buildQueryCodeAndName(String tableName, String code, String name) {
        StringBuilder str = new StringBuilder();
        str.append("select " + code + " as code,");
        str.append(name + " as name ");
        str.append(" from " + tableName);
        str.append(" where fidata_del_flag=1 ");
        return str.toString();
    }

    @Override
    public List<OperatorVO> getOperatorList() {
        return null;
    }

    @Override
    public String buildOperatorCondition(List<FilterQueryDTO> operators) {
        return "";
    }

    @Override
    public String buildInsertSingleData(Map<String, Object> data, String tableName) {
        StringBuilder str = new StringBuilder();
        str.append("insert into " + tableName);
        str.append("(");
        List<String> columnList = new ArrayList<>();
        List<String> valueList = new ArrayList<>();
        for (Map.Entry<String, Object> item : data.entrySet()) {
            columnList.add(item.getKey());
            valueList.add(item.getValue().toString());
        }
        str.append(String.join(",", columnList) + ") ");
        str.append("values(" + CommonMethods.convertListToString(valueList) + ")");
        return str.toString();
    }

}
