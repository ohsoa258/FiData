package com.fisk.task.listener.postgre.datainput;

import com.fisk.common.core.enums.task.SynchronousTypeEnum;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.fisk.task.dto.task.BuildPhysicalTableDTO;


import java.util.List;

/**
 * @author cfk
 */
public interface IbuildTable {
    /**
     * 创建stg和ods表
     *
     * @param buildPhysicalTableDTO buildPhysicalTableDTO
     * @return List<String>  返回两条建表语句
     */
    List<String> buildStgAndOdsTable(BuildPhysicalTableDTO buildPhysicalTableDTO);

    /**
     * 查询数据库应该创建表数量
     *
     * @param buildPhysicalTableDTO 配置信息
     * @return String  查询数据库应该创建表数量语句
     */
    String queryTableNum(BuildPhysicalTableDTO buildPhysicalTableDTO);


    /**
     * assemblySql
     *
     * @param config              配置信息
     * @param synchronousTypeEnum synchronousTypeEnum
     * @param funcName            funcName
     * @return String
     */
    String assemblySql(DataAccessConfigDTO config, SynchronousTypeEnum synchronousTypeEnum, String funcName, BuildNifiFlowDTO buildNifiFlow);

    /**
     * 获取版本修改语句的函数执行语句
     *
     * @return String
     */
    String prepareCallSql();

    /**
     * 查询同步数据条数语句
     *
     * @return String
     */
    String queryNumbersField(BuildNifiFlowDTO dto, DataAccessConfigDTO config);

    /**
     * tableName 一般是简称_表名,或简称.表名,如果是.要从中间切开,简称.stg_表名.或者stg_简称_表名
     *
     * @return String
     */
    List<String> getStgAndTableName(String tableName);
}
