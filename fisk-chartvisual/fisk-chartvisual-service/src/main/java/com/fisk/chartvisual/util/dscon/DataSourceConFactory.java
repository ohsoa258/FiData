package com.fisk.chartvisual.util.dscon;

import com.fisk.common.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;

public class DataSourceConFactory {

    public static AbstractUseDataBase getConnection(DataSourceTypeEnum type){
        switch (type){
            case SQLSERVER:
                return new UseSqlServerDataBase();
            case MYSQL:
                return new UseMySqlDataBase();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

}
