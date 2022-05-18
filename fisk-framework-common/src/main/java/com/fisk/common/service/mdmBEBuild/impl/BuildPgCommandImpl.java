package com.fisk.common.service.mdmBEBuild.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.enums.mdm.ImportDataEnum;
import com.fisk.common.service.mdmBEBuild.CommonMethods;
import com.fisk.common.service.mdmBEBuild.IBuildSqlCommand;
import com.fisk.common.service.mdmBEBuild.dto.ImportDataPageDTO;
import com.fisk.common.service.mdmBEBuild.dto.InsertImportDataDTO;
import com.fisk.common.service.mdmBEBuild.dto.MasterDataPageDTO;
import com.google.common.base.Joiner;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * @author WangYan
 * @date 2022/4/13 18:06
 */
public class BuildPgCommandImpl implements IBuildSqlCommand {

    @Override
    public String buildAttributeLogTable(String tableName) {
        StringBuilder str = new StringBuilder();
        str.append("CREATE TABLE public." + tableName).append("(");
        str.append("ID int4 NOT NULL,");
        str.append("model_id int4 NULL,");
        str.append("entity_id int4 NULL,");
        str.append("attribute_id int4 NULL,");
        str.append("member_id int4 NULL,");
        str.append("batch_id int4 NULL,");
        str.append("version_id int4 NULL,");
        str.append("old_code VARCHAR ( 200 ) NULL,");
        str.append("old_value VARCHAR ( 200 ) NULL,");
        str.append("new_code VARCHAR ( 200 ) NULL,");
        str.append("new_value VARCHAR ( 200 ) NULL");
        str.append(");");
        return str.toString();
    }

    @Override
    public String buildInsertImportData(InsertImportDataDTO dto) {
        Date date = new Date();
        StringBuilder str = new StringBuilder();
        str.append("insert into " + dto.getTableName());
        str.append("(" + CommonMethods.getColumnNameAndValue(dto.getMembers().get(0), ImportDataEnum.COLUMN_NAME.getValue()));
        str.append(",fidata_import_type,fidata_batch_code,fidata_version_id,");
        str.append("fidata_create_time,fidata_create_user,fidata_update_time,fidata_update_user,fidata_del_flag");
        str.append(")");
        str.append(" values(" + CommonMethods.getColumnNameAndValue(dto.getMembers().get(0), ImportDataEnum.COLUMN_VALUE.getValue()) + ","
                + dto.getImportType() + ",'" + dto.getBatchCode() + "'," + dto.getVersionId() + ",'");
        str.append(CommonMethods.getFormatDate(date) + "'," + dto.getUserId() + ",'");
        str.append(CommonMethods.getFormatDate(date) + "'," + dto.getUserId() + ",1" + ")");
        if (dto.getMembers().size() > 1) {
            for (int i = 1; i < dto.getMembers().size(); i++) {
                str.append(",(" + CommonMethods.getColumnNameAndValue(dto.getMembers().get(i), ImportDataEnum.COLUMN_VALUE.getValue()) + ","
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
        int offset = (dto.getPageIndex() - 1) * dto.getPageSize();
        StringBuilder str = new StringBuilder();
        str.append("select * from " + dto.getTableName());
        str.append(" where 1=1 ");
        if (!StringUtils.isEmpty(dto.getBatchCode())) {
            str.append(" and fidata_batch_code='" + dto.getBatchCode() + "'");
        }
        if (!CollectionUtils.isEmpty(dto.getStatus())) {
            str.append(" and fidata_status in(" + Joiner.on(",").join(dto.getStatus()) + ")");
        }
        if (!CollectionUtils.isEmpty(dto.getSyncType())) {
            str.append(" and fidata_syncy_type in(" + Joiner.on(",").join(dto.getSyncType()) + ")");
        }
        str.append(" limit " + dto.getPageSize() + " offset " + offset);
        return str.toString();
    }

    @Override
    public String buildUpdateImportData(Map<String, Object> jsonObject,
                                        String tableName,
                                        int importType) {
        StringBuilder str = new StringBuilder();
        str.append("update " + tableName);
        str.append(" set fidata_import_type=" + importType);
        Iterator iter = jsonObject.entrySet().iterator();
        String primaryKey = null;
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            if ("fidata_id".equals(entry.getKey().toString())) {
                primaryKey = entry.getValue().toString();
                continue;
            } else if ("fidata_import_type".equals(entry.getKey().toString())) {
                continue;
            }
            if (entry.getValue() == null) {
                str.append("," + entry.getKey().toString() + "=null");
                continue;
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
        //计算偏移量
        int offset = (dto.getPageIndex() - 1) * dto.getPageSize();
        StringBuilder str = new StringBuilder();
        str.append("select " + dto.getColumnNames());
        str.append(" from " + dto.getTableName() + " view ");
        str.append("where fidata_del_flag = 1 and fidata_version_id = " + dto.getVersionId());
        str.append(" order by fidata_create_time,fidata_id desc ");
        str.append(" limit " + dto.getPageSize() + " offset " + offset);
        return str.toString();
    }

}
