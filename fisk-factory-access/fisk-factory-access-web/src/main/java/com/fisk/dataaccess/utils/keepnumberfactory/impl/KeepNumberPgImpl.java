package com.fisk.dataaccess.utils.keepnumberfactory.impl;

import com.fisk.dataaccess.dto.table.TableKeepNumberDTO;
import com.fisk.dataaccess.entity.AppRegistrationPO;
import com.fisk.dataaccess.utils.keepnumberfactory.IBuildKeepNumber;

import java.util.List;

public class KeepNumberPgImpl implements IBuildKeepNumber {

    @Override
    public String setKeepNumberSql(TableKeepNumberDTO dto, AppRegistrationPO appRegistrationPO,List<String> stgAndTableName) {
        //临时表
        String stgTableName = "";
        //目标表
        String odsTableName = "";
        for (int i = 0; i < 2; i++) {
            if (i == 0) {
                stgTableName = stgAndTableName.get(i);
            } else {
                odsTableName = stgAndTableName.get(i);
            }
        }

        if (appRegistrationPO.whetherSchema) {
            stgTableName = "\"" + appRegistrationPO.appAbbreviation + "\"" + "." + "\"" + stgTableName + "\"";
            odsTableName = "\"" + appRegistrationPO.appAbbreviation + "\"" + "." + "\"" + odsTableName + "\"";
        } else {
            stgTableName = "\"dbo\"." + "\"" + stgTableName + "\"";
            odsTableName = "\"dbo\"." + "\"" + odsTableName + "\"";
        }

        //获取keepNumber
        String keepNumber = dto.keepNumber;
        //日期范围
        String[] kNumber = keepNumber.split(" ");
        String dateRange = kNumber[0];
        //日期单位   去除头尾空格,变为大写
        String dateUnit = kNumber[1].toUpperCase();

        StringBuilder delSql = new StringBuilder("DELETE FROM ");
        //为sql拼接stg表名和where条件
        delSql.append(stgTableName)
                .append(" WHERE fi_createtime::Date<(CURRENT_TIMESTAMP - INTERVAL")
                .append("'")
                .append(keepNumber)
                .append("s")
                .append("');");
        return String.valueOf(delSql);
    }
}
