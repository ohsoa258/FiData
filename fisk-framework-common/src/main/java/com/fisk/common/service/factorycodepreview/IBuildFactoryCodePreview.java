package com.fisk.common.service.factorycodepreview;


import com.fisk.common.service.factorycodepreview.factorycodepreviewdto.PreviewTableBusinessDTO;
import com.fisk.common.service.factorycodepreview.factorycodepreviewdto.PublishFieldDTO;

import java.util.List;

/**
 * @author lishiji
 */
public interface IBuildFactoryCodePreview {

    /**
     * 追加覆盖方式拼接的sql代码
     *
     * @param tableName       真实表名
     * @param sourceTableName 来源表名（临时表名）
     * @param fieldList       前端传递的源表字段属性集合
     * @return
     */
    String insertAndSelectSql(String tableName, String sourceTableName, List<PublishFieldDTO> fieldList, String updateSql);

    /**
     * 全量覆盖方式拼接的sql代码
     *
     * @param tableName       真实表名
     * @param sourceTableName 来源表名（临时表名）
     * @param fieldList       前端传递的源表字段属性集合
     * @return
     */
    String fullVolumeSql(String tableName, String sourceTableName, List<PublishFieldDTO> fieldList, String updateSql);

    /**
     * 业务标识覆盖方式--删除插入--拼接的sql代码
     *
     * @param tableName       真实表名
     * @param sourceTableName 来源表名（临时表名）
     * @param fieldList       前端传递的源表字段属性集合
     * @return
     */
    String delAndInsert(String tableName, String sourceTableName, List<PublishFieldDTO> fieldList,Integer type, String updateSql);

    /**
     * 业务标识覆盖方式--merge覆盖（业务标识可以作为业务主键）--拼接的sql代码
     *
     * @param tableName       真实表名
     * @param sourceTableName 来源表名（临时表名）
     * @param fieldList       前端传递的源表字段属性集合
     * @return
     */
    String merge(String tableName, String sourceTableName, List<PublishFieldDTO> fieldList,Integer type, String updateSql);

    /**
     * 业务时间覆盖方式拼接的sql代码
     *
     * @param tableName               真实表名
     * @param sourceTableName         来源表名（临时表名）
     * @param fieldList               前端传递的源表字段属性集合
     * @param previewTableBusinessDTO 业务时间覆盖方式页面选择的逻辑
     * @return
     */
    String businessTimeOverLay(String tableName, String sourceTableName,
                                             List<PublishFieldDTO> fieldList, PreviewTableBusinessDTO previewTableBusinessDTO, String updateSql);

    /**
     * 标识覆盖拼接的sql代码
     * 标识出在上游被物理删除的数据
     * @param tableName
     * @param sourceTableName
     * @param fieldList
     * @param type
     * @param updateSql
     * @return
     */
    String mergeWithMark(String tableName, String sourceTableName, List<PublishFieldDTO> fieldList, Integer type, String updateSql);
}
