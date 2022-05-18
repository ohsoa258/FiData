package com.fisk.common.service.mdmBEBuild.impl;

import com.fisk.common.service.mdmBEBuild.CommonMethods;
import com.fisk.common.service.mdmBEBuild.IBuildSqlCommand;
import com.fisk.common.service.mdmBEBuild.dto.ImportDataPageDTO;
import com.fisk.common.service.mdmBEBuild.dto.InsertImportDataDTO;
import com.fisk.common.service.mdmBEBuild.dto.MasterDataPageDTO;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

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
    public String buildUpdateImportData(Map<String, Object> jsonObject, String tableName, int importType) {
        StringBuilder str = new StringBuilder();
        str.append("update " + tableName);
        str.append(" set fidata_import_type=" + importType);
        Iterator iter = jsonObject.entrySet().iterator();
        String primaryKey = null;
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            if ("fidata_id".equals(entry.getKey().toString())) {
                primaryKey = entry.getValue().toString();
            }
            str.append("," + entry.getKey().toString() + "='" + entry.getValue().toString() + "'");
        }
        if (StringUtils.isEmpty(primaryKey)) {
            return "";
        }
        str.append(" where fidata_id=" + primaryKey);
        return str.toString();
    }

    @Override
    public String buildMasterDataPage(MasterDataPageDTO dto) {
        return "";
    }


}
