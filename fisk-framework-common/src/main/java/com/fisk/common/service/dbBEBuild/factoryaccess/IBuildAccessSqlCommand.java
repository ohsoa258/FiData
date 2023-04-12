package com.fisk.common.service.dbBEBuild.factoryaccess;

import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.factory.BusinessTimeEnum;
import com.fisk.common.service.dbBEBuild.factoryaccess.dto.DataTypeConversionDTO;
import com.fisk.common.service.dbBEBuild.factoryaccess.dto.TableBusinessTimeDTO;

/**
 * @author JianWenYang
 */
public interface IBuildAccessSqlCommand {

    /**
     * cdc类型获取已存在表sql
     *
     * @return
     */
    String buildUseExistTable();

    /**
     * 基于sql,只取前几条数据sql
     *
     * @param sql
     * @param pageSize
     * @param offset
     * @return
     */
    String buildPaging(String sql, Integer pageSize, Integer offset);

    /**
     * @description 创建版本
     * @author dick
     * @date 2022/11/2 10:30
     * @version v1.0
     * @params
     * @return java.lang.String
     */
    String buildVersionSql(String type,String value);

    /**
     * @return java.lang.String
     * @description 获取周数
     * @author dick
     * @date 2022/11/2 18:49
     * @version v1.0
     * @params
     */
    String buildWeekSql(String date);

    /**
     * @description 判断表是否存在
     * @author dick
     * @date 2022/11/3 17:33
     * @version v1.0
     * @params tableName
     * @return boolean
     */
    String buildExistTableSql(String tableName);

    /**
     * 查询数据库时间
     *
     * @param timeEnum
     * @return
     */
    String buildQueryTimeSql(BusinessTimeEnum timeEnum);

    /**
     * 业务时间覆盖条件语句
     *
     * @param dto
     * @param businessDate
     * @return
     */
    String buildBusinessCoverCondition(TableBusinessTimeDTO dto, Integer businessDate);

    /**
     * 数据类型转换
     *
     * @param dto
     * @param typeEnum
     * @return
     */
    String[] dataTypeConversion(DataTypeConversionDTO dto, DataSourceTypeEnum typeEnum);

    /**
     * 数据类型下拉集合
     *
     * @return
     */
    JSONObject dataTypeList();

    /**
     * @return java.lang.String
     * @description 获取版本删除语句（不含条件）
     * @author dick
     * @date 2022/11/2 18:49
     * @version v1.0
     * @params
     */
    String buildVersionDeleteSql(String tableName);
}
