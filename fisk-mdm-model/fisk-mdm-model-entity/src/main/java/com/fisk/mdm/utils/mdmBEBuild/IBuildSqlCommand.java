package com.fisk.mdm.utils.mdmBEBuild;

import com.fisk.mdm.dto.attribute.AttributeFactDTO;
import com.fisk.mdm.vo.entity.EntityInfoVO;

import java.util.List;

/**
 * @author WangYan
 * @date 2022/4/13 18:05
 * 构建sql命令
 */
public interface IBuildSqlCommand {

    /**
     * 创建属性日志表
     * @param tableName
     * @return
     */
    String buildAttributeLogTable(String tableName);

    /**
     * 创建Stg表sql
     * @param entityInfoVo
     * @param tableName
     * @return
     */
    String buildStgTable(EntityInfoVO entityInfoVo,String tableName);

    /**
     * 创建日志表
     * @param entityInfoVo
     * @param tableName
     * @param code
     * @return
     */
    String buildLogTable(EntityInfoVO entityInfoVo,String tableName,String code);

    /**
     * 创建表mdm
     * @param entityInfoVo
     * @param tableName
     * @param code
     * @return
     */
    String buildMdmTable(EntityInfoVO entityInfoVo,String tableName,String code);

    /**
     * 修改字段类型
     * @param tableName
     * @param filedName
     * @param type
     * @return
     */
    String modifyFieldType(String tableName,String filedName,String type);

    /**
     * 修改字段长度
     * @param tableName
     * @param filedName
     * @param type
     * @return
     */
    String modifyFieldLength(String tableName,String filedName,String type);

    /**
     * 删除表
     * @param tableName
     * @return
     */
    String dropTable(String tableName);

    /**
     * 删除视图
     * @param viwName
     * @return
     */
    String dropViw(String viwName);

    /**
     * 添加字段
     * @param tableName
     * @param filedName
     * @param filedType
     * @return
     */
    String addColumn(String tableName,String filedName,String filedType);

    /**
     * 设置字段不可为空
     * @param tableName
     * @param filedName
     * @return
     */
    String notNullable(String tableName,String filedName);

    /**
     * 设置字段可为空
     * @param tableName
     * @param filedName
     * @return
     */
    String nullable(String tableName,String filedName);

    /**
     * 删除字段
     * @param tableName
     * @param filedName
     * @return
     */
    String deleteFiled(String tableName,String filedName);

    /**
     * 查询数据
     * @param tableName
     * @return
     */
    String queryData(String tableName);

    /**
     * 插入属性事实表
     * @param dtoList
     * @return
     */
    String insertAttributeFact(List<AttributeFactDTO> dtoList);

    /**
     * 删除数据(物理删除)
     * @param tableName
     * @param deleteFiled
     * @param attributeIds
     * @return
     */
    String deleteDataByAttributeId(String tableName,String deleteFiled,List<Integer> attributeIds);
}
