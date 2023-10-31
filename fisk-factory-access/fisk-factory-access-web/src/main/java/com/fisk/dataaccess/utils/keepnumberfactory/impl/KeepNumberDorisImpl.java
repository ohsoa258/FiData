package com.fisk.dataaccess.utils.keepnumberfactory.impl;

import com.fisk.dataaccess.dto.table.TableKeepNumberDTO;
import com.fisk.dataaccess.entity.AppRegistrationPO;
import com.fisk.dataaccess.utils.keepnumberfactory.IBuildKeepNumber;

import java.util.List;

public class KeepNumberDorisImpl implements IBuildKeepNumber {

    @Override
    public String setKeepNumberSql(TableKeepNumberDTO dto, AppRegistrationPO appRegistrationPO,List<String> stgAndTableName) {
        //临时表
        String stgTableName = "";
        //目标表
        for (int i = 0; i < 2; i++) {
            if (i == 0) {
                stgTableName = stgAndTableName.get(i);
            }
        }
        StringBuilder delSql = new StringBuilder("TRUNCATE TABLE ");
        //为sql拼接stg表名和where条件
        delSql.append(stgTableName)
                .append(";");
        return String.valueOf(delSql);
    }
}
