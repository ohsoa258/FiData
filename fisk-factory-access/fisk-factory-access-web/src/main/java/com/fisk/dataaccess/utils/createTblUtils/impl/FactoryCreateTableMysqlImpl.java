package com.fisk.dataaccess.utils.createTblUtils.impl;

import com.fisk.dataaccess.dto.datasource.DataSourceFullInfoDTO;
import com.fisk.dataaccess.entity.TableFieldsPO;
import com.fisk.dataaccess.utils.createTblUtils.IBuildCreateTableFactory;

import java.util.List;

public class FactoryCreateTableMysqlImpl implements IBuildCreateTableFactory {

    @Override
    public String createTable(String tableName, List<TableFieldsPO> fieldList) {
        return null;
    }

    @Override
    public String checkTableIfNotExists() {
        return null;
    }

    @Override
    public String createSourceSql(String tableName, List<TableFieldsPO> fieldList, DataSourceFullInfoDTO dto) {
        return null;
    }

    @Override
    public String createSinkSql(String tableName, List<TableFieldsPO> fieldList, DataSourceFullInfoDTO dto) {
        return null;
    }

    @Override
    public String createInsertSql(String tableName, List<TableFieldsPO> fieldList, DataSourceFullInfoDTO dto) {
        return null;
    }

}
