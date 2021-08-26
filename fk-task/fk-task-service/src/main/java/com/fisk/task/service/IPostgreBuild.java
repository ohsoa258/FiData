package com.fisk.task.service;

import com.fisk.common.entity.BusinessResult;

public interface IPostgreBuild {

     /**
      * executsql   执行的sql语句
      * businessType   执行的数据库
      * @return 返回语句执行结果
      */
     BusinessResult postgreBuildTable(String executsql,String businessType);

     /**
      * executsql   执行的sql语句
      * businessType   执行的数据库
      * 传入要返回数据类型,不可为null
      * @return 返回执行sql查到的结果与sql执行结果
      */
     <T> BusinessResult postgreQuery(String executsql,String businessType,T data);
}
