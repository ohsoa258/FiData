package com.fisk.common.service.dbBEBuild.factoryaccess.impl;

import com.fisk.common.service.dbBEBuild.factoryaccess.IBuildAccessSqlCommand;

/**
 * @author JianWenYang
 */
public class BuildAccessPgCommandImpl implements IBuildAccessSqlCommand {

    @Override
    public String buildUseExistTable() {
        return null;
    }

    @Override
    public String buildPaging(String sql, Integer pageSize, Integer offset) {
        StringBuilder str = new StringBuilder();
        str.append(sql);
        str.append(" limit " + pageSize + " offset " + offset);
        return str.toString();
    }
}
