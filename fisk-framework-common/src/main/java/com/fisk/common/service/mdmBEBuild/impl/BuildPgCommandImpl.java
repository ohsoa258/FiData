package com.fisk.common.service.mdmBEBuild.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.enums.mdm.ImportDataEnum;
import com.fisk.common.core.enums.mdm.OperatorEnum;
import com.fisk.common.service.mdmBEBuild.CommonMethods;
import com.fisk.common.service.mdmBEBuild.IBuildSqlCommand;
import com.fisk.common.service.mdmBEBuild.dto.ImportDataPageDTO;
import com.fisk.common.service.mdmBEBuild.dto.InsertImportDataDTO;
import com.fisk.common.service.mdmBEBuild.dto.MasterDataPageDTO;
import com.fisk.common.service.pageFilter.dto.FilterQueryDTO;
import com.fisk.common.service.pageFilter.dto.OperatorVO;
import com.google.common.base.Joiner;
import org.apache.commons.lang.StringUtils;

import java.util.*;

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
        int delFlat = dto.getDelete() ? 0 : 1;
        StringBuilder str = new StringBuilder();
        str.append("insert into " + dto.getTableName());
        str.append("(" + CommonMethods.getColumnNameAndValue(dto.getMembers().get(0), ImportDataEnum.COLUMN_NAME.getValue()));
        str.append(",fidata_import_type,fidata_batch_code,fidata_version_id,");
        str.append("fidata_create_time,fidata_create_user,fidata_update_time,fidata_update_user,fidata_del_flag");
        str.append(")");
        str.append(" values(" + CommonMethods.getColumnNameAndValue(dto.getMembers().get(0), ImportDataEnum.COLUMN_VALUE.getValue()) + ","
                + dto.getImportType() + ",'" + dto.getBatchCode() + "'," + dto.getVersionId() + ",'");
        str.append(CommonMethods.getFormatDate(date) + "'," + dto.getUserId() + ",'");
        str.append(CommonMethods.getFormatDate(date) + "'," + dto.getUserId() + "," + delFlat + ")");
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
    public String buildUpdateImportData(Map<String, Object> jsonObject, String tableName) {
        StringBuilder str = new StringBuilder();
        str.append("update " + tableName);
        str.append(" set fidata_import_type=" + jsonObject.get("fidata_import_type"));
        jsonObject.remove("fidata_import_type");
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
        //计算偏移量
        int offset = (dto.getPageIndex() - 1) * dto.getPageSize();
        StringBuilder str = new StringBuilder();
        str.append("select " + dto.getColumnNames());
        str.append(" from " + dto.getTableName() + " view ");
        str.append("where fidata_del_flag = 1 and fidata_version_id = " + dto.getVersionId());
        if (!StringUtils.isEmpty(dto.getConditions())) {
            str.append(dto.getConditions());
        }
        str.append(" order by fidata_id desc ");
        if (!dto.getExport()) {
            str.append(" limit " + dto.getPageSize() + " offset " + offset);
        }
        return str.toString();
    }

    @Override
    public String buildVerifyRepeatCode(String tableName, String batchCode) {
        StringBuilder str = new StringBuilder();
        //更改重复的code上传状态和错误描述
        str.append("update " + tableName);
        str.append(" set fidata_status=3,fidata_error_msg='" + "编码重复" + "'");
        str.append(" where fidata_batch_code='" + batchCode + "' and code in");
        str.append("(select code from " + tableName);
        str.append(" where fidata_batch_code='" + batchCode + "'");
        str.append("and code <>'' and code is not null GROUP BY code HAVING count(*)>1);");
        //更改没有重复code上传状态和错误描述
        str.append("update " + tableName);
        str.append(" set fidata_status=0,fidata_error_msg=null");
        str.append(" where fidata_batch_code='" + batchCode + "' and code in");
        str.append("(select code from " + tableName);
        str.append(" where fidata_batch_code='" + batchCode + "'");
        str.append("and code <>'' and code is not null and fidata_error_msg='编码重复' GROUP BY code HAVING count(*)=1)");
        return str.toString();
    }

    @Override
    public String buildQueryOneColumn(String tableName, String selectColumnName) {
        return "select distinct " + selectColumnName + " as columns from " + tableName;
    }

    @Override
    public String buildQueryCount(String tableName, String queryConditions) {
        StringBuilder str = new StringBuilder();
        str.append("SELECT COUNT(*) AS totalNum FROM " + tableName);
        str.append(" WHERE fidata_del_flag=1 ");
        if (!StringUtils.isEmpty(queryConditions)) {
            str.append(queryConditions);
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
        str.append(" where 1=1 " + queryConditions);
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
        List<OperatorVO> data;
        String publicUserType = "\"useType\": [\n" +
                "      \"DATE\",\n" +
                "      \"TIME\",\n" +
                "      \"TIMESTAMP\",\n" +
                "      \"NUMERICAL\",\n" +
                "      \"TEXT\",\n" +
                "      \"FLOAT\",\n" +
                "      \"MONEY\",\n" +
                "      \"DOMAIN\"\n" +
                "    ]\n";
        String jsonStr = "[\n" +
                "  {\n" +
                "    \"label\": \"不为NULL\",\n" +
                "    \"value\": \"不为NULL\",\n" +
                "    \"operators\": \"IS NOT NULL\",\n" +
                publicUserType +
                "  },\n" +
                "  {\n" +
                "    \"label\": \"为NULL\",\n" +
                "    \"value\": \"为NULL\",\n" +
                "    \"operators\": \"IS NULL\",\n" +
                publicUserType +
                "  },\n" +
                "  {\n" +
                "    \"label\": \"等于\",\n" +
                "    \"value\": \"等于\",\n" +
                "    \"operators\": \"=\",\n" +
                publicUserType +
                "  },\n" +
                "  {\n" +
                "    \"label\": \"不等于\",\n" +
                "    \"value\": \"不等于\",\n" +
                "    \"operators\": \"<>\",\n" +
                publicUserType +
                "  },\n" +
                "  {\n" +
                "    \"label\": \"小于\",\n" +
                "    \"value\": \"小于\",\n" +
                "    \"operators\": \"<\",\n" +
                publicUserType +
                "  },\n" +
                "  {\n" +
                "    \"label\": \"小于或等于\",\n" +
                "    \"value\": \"小于或等于\",\n" +
                "    \"operators\": \"<=\",\n" +
                publicUserType +
                "  },\n" +
                "  {\n" +
                "    \"label\": \"大于\",\n" +
                "    \"value\": \"大于\",\n" +
                "    \"operators\": \">\",\n" +
                publicUserType +
                "  },\n" +
                "  {\n" +
                "    \"label\": \"大于或等于\",\n" +
                "    \"value\": \"大于或等于\",\n" +
                "    \"operators\": \">=\",\n" +
                publicUserType +
                "  },\n" +
                "  {\n" +
                "    \"label\": \"类似于\",\n" +
                "    \"value\": \"类似于\",\n" +
                "    \"operators\": \"LIKE\",\n" +
                "    \"useType\": [\n" +
                "      \"TEXT\"\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"label\": \"不类似于\",\n" +
                "    \"value\": \"不类似于\",\n" +
                "    \"operators\": \"NOT LIKE\",\n" +
                "    \"useType\": [\n" +
                "      \"TEXT\"\n" +
                "    ]\n" +
                "  }\n" +
                "]";
        data = JSONObject.parseArray(jsonStr, OperatorVO.class);
        return data;
    }

    @Override
    public String buildOperatorCondition(List<FilterQueryDTO> operators) {
        StringBuilder str = new StringBuilder();
        for (FilterQueryDTO item : operators) {
            OperatorEnum operatorEnum = OperatorEnum.getValue(item.getQueryType());
            switch (operatorEnum) {
                case IS_NULL:
                case NOT_NULL:
                    str.append(" and " + item.columnName + " " + operatorEnum.getValue());
                    break;
                case EQUAL:
                case NOT_EQUAL:
                case LESS_THAN:
                case LESS_THAN_EQUAL:
                case GREATER_THAN:
                case GREATER_THAN_EQUAL:
                    str.append(" and " + item.columnName + " " + operatorEnum.getValue() + "'" + item.columnValue + "'");
                    break;
                case LIKE:
                case NOT_LIKE:
                    str.append(" and " + item.columnName + " " + operatorEnum.getValue() + "'" + item.columnValue + "%'");
                    break;
                default:
                    continue;
            }
        }
        return str.toString();
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
            valueList.add(item.getValue() == null ? "null" : item.getValue().toString());
        }
        str.append(String.join(",", columnList) + ") ");
        str.append("values(" + CommonMethods.convertListToString(valueList) + ")");
        return str.toString();
    }

}
