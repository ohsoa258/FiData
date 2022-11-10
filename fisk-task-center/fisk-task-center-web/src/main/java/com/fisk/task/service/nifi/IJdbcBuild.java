package com.fisk.task.service.nifi;

import com.fisk.common.core.baseObject.entity.BusinessResult;
import com.fisk.common.core.enums.task.BusinessTypeEnum;
import com.fisk.task.dto.doris.UpdateLogAndImportDataDTO;

import java.sql.ResultSet;

public interface IJdbcBuild {

     /**
      * executsql   执行的sql语句
      * businessType   执行的数据库
      * @return 返回语句执行结果
      */
     BusinessResult postgreBuildTable(String executsql, BusinessTypeEnum businessTypeEnum);

     /**
      * executsql   执行的sql语句
      * businessType   执行的数据库
      * 传入要返回数据类型,不可为null
      * @return 返回执行sql查到的结果与sql执行结果
      */
     <T> BusinessResult postgreQuery(String executsql,BusinessTypeEnum businessTypeEnum,T data);

     /**
      * executsql   执行的sql语句
      * businessType   执行的数据库
      * @return 返回执行sql查到的结果与sql执行结果
      */
     public BusinessResult<ResultSet> postgreQuery(String executsql, BusinessTypeEnum businessTypeEnum);

     /**
      * pgsql stg表数据同步到ods
      * @param stgTable stg表
      * @param odsTable ods表
      * @return
      */
     BusinessResult postgreDataStgToOds(String stgTable, String odsTable,UpdateLogAndImportDataDTO dto);
}
