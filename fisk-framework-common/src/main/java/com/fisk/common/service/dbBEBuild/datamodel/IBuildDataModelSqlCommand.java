package com.fisk.common.service.dbBEBuild.datamodel;

import com.alibaba.fastjson.JSONObject;
import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceRelationsDTO;
import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceTableConfigDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IBuildDataModelSqlCommand {

    /**
     * 拼接表名
     *
     * @param entity
     * @return
     */
    String buildAppendField(List<TableSourceTableConfigDTO> entity);

    /**
     * 拼接关联表
     *
     * @param relations
     * @return
     */
    String buildAppendRelationTable(List<TableSourceRelationsDTO> relations);

    /**
     * 拼接默认获取前几天数据
     *
     * @param sql
     * @param pageSize
     * @return
     */
    String buildPageSql(String sql, Integer pageSize);

    /**
     * 拼接查询表数据sql
     *
     * @param relations
     * @param tableName
     * @param jsonObject
     * @return
     */
    String buildSelectTable(List<TableSourceRelationsDTO> relations, String tableName, JSONObject jsonObject);

    /**
     * 构建时间维度表创表语句
     *
     * @param tableName
     * @return
     */
    String buildTimeDimensionCreateTable(String tableName);

}
