package com.fisk.common.service.mdmBEBuild.impl;

import com.fisk.common.core.enums.mdm.ImportDataEnum;
import com.fisk.common.service.mdmBEBuild.CommonMethods;
import com.fisk.common.service.mdmBEBuild.IBuildSqlCommand;
import com.fisk.common.service.mdmBEBuild.dto.InsertImportDataDTO;
import com.fisk.common.service.mdmBEBuild.dto.PageDataDTO;
import org.apache.commons.lang.StringUtils;

import java.util.Date;

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
    public String buildPageData(PageDataDTO dto) {
        int offset = (dto.getPageIndex() - 1) * dto.getPageSize();
        StringBuilder str = new StringBuilder();
        str.append("select * from " + dto.getTableName());
        if (!StringUtils.isEmpty(dto.getConditions())) {
            str.append(dto.getConditions());
        }
        str.append(" limit " + dto.getPageSize() + " offset " + offset);
        return str.toString();
    }

}
