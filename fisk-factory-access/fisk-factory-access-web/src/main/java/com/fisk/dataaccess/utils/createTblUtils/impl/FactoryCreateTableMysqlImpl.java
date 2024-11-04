package com.fisk.dataaccess.utils.createTblUtils.impl;

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

}
