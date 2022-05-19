package com.fisk.mdm.utils.mdmBEBuild.impl;

import com.fisk.mdm.vo.entity.EntityInfoVO;
import com.fisk.mdm.utils.mdmBEBuild.IBuildSqlCommand;

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
    public String buildStgTable(EntityInfoVO entityInfoVo,String tableName) {
        return null;
    }

    @Override
    public String buildMdmTable(EntityInfoVO entityInfoVo,String tableName,String code) {
        return null;
    }

    @Override
    public String modifyFieldType(String tableName, String filedName, String type) {
        return null;
    }

    @Override
    public String modifyFieldLength(String tableName, String filedName, String type) {
        return null;
    }

    @Override
    public String dropTable(String tableName) {
        return null;
    }

    @Override
    public String dropViw(String viwName) {
        return null;
    }

    @Override
    public String addColumn(String tableName, String filedName, String filedType) {
        return null;
    }

    @Override
    public String notNullable(String tableName, String filedName) {
        return null;
    }

    @Override
    public String nullable(String tableName, String filedName) {
        return null;
    }

    @Override
    public String deleteFiled(String tableName, String filedName) {
        return null;
    }

    @Override
    public String queryData(String tableName) {
        return null;
    }
}
