package com.fisk.task.utils.buildSql.impl;

import com.fisk.task.utils.buildSql.IBuildSqlCommand;

/**
 * @author WangYan
 * @date 2022/4/13 18:06
 */
public class BuildPgCommandImpl implements IBuildSqlCommand {

    @Override
    public String buildAttributeLog(String tableName) {
        StringBuilder str = new StringBuilder();
        str.append("CREATE TABLE public." + tableName).append("(");
        str.append("ID int4 NOT NULL,");
        str.append("model_id int4 NULL,");
        str.append("entity_id int4 NULL,");
        str.append("attribute_id int4 NULL,");
        str.append("member_id int4 NULL,");
        str.append("batch_id int4 NULL,");
        str.append("version_id int4 NULL,");
        str.append("old_code VARCHAR ( 50 ) NULL,");
        str.append("old_value VARCHAR ( 50 ) NULL,");
        str.append("new_code VARCHAR ( 50 ) NULL,");
        str.append("new_value VARCHAR ( 50 ) NULL");
        str.append(");");
        return str.toString();
    }
}
