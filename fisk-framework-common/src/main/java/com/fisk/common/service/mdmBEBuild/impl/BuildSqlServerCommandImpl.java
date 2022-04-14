package com.fisk.common.service.mdmBEBuild.impl;

import com.fisk.common.service.mdmBEBuild.IBuildSqlCommand;

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
}
