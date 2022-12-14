package com.fisk.common.service.sqlparser;

import com.alibaba.druid.DbType;
import com.fisk.common.service.sqlparser.model.TableMetaDataObject;

import java.util.List;

/**
 * @author gy
 * @version 1.0
 * @description SQL解析器 接口方法
 * @date 2022/12/6 17:20
 */
public interface ISqlParser {

    /**
     * 传递sql查询语句，返回查询语句中出现的底层数据表
     *
     * @param sql    sql语句
     * @param dbType 数据库类型
     * @return 表对象信息
     * @throws 解析失败
     */
    List<TableMetaDataObject> getDataTableBySql(String sql, DbType dbType) throws Exception;

}
