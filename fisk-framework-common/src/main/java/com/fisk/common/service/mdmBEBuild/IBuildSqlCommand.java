package com.fisk.common.service.mdmBEBuild;

import com.fisk.common.service.mdmBEBuild.dto.ImportDataPageDTO;
import com.fisk.common.service.mdmBEBuild.dto.InsertImportDataDTO;
import com.fisk.common.service.mdmBEBuild.dto.MasterDataPageDTO;
import com.fisk.common.service.pageFilter.dto.FilterQueryDTO;
import com.fisk.common.service.pageFilter.dto.OperatorVO;

import java.util.List;
import java.util.Map;

/**
 * @author WangYan
 * @date 2022/4/13 18:05
 * 构建sql命令
 */
public interface IBuildSqlCommand {

    /**
     * 创建属性日志表
     *
     * @param tableName
     * @return
     */
    String buildAttributeLogTable(String tableName);

    /**
     * 主数据维护-导入模板数据生成insert语句
     *
     * @param dto
     * @return
     */
    String buildInsertImportData(InsertImportDataDTO dto);

    /**
     * 获取分页数据
     *
     * @param dto
     * @return
     */
    String buildImportDataPage(ImportDataPageDTO dto);

    /**
     * 主数据维护-导入模板数据实时修改
     *
     * @param jsonObject
     * @param tableName
     * @return
     */
    String buildUpdateImportData(Map<String, Object> jsonObject, String tableName);

    /**
     * 主数据维护-首页列表
     *
     * @param dto
     * @return
     */
    String buildMasterDataPage(MasterDataPageDTO dto);

    /**
     * 主数据维护--验证重复code生成update语句
     *
     * @param tableName
     * @param batchCode
     * @return
     */
    String buildVerifyRepeatCode(String tableName, String batchCode);

    /**
     * 查询表中某个字段的数据
     *
     * @param tableName
     * @param selectColumnName
     * @param versionId
     * @return
     */
    String buildQueryOneColumn(String tableName, String selectColumnName, int versionId);

    /**
     * 查询条数
     *
     * @param tableName
     * @param queryConditions 查询条件
     * @return
     */
    String buildQueryCount(String tableName, String queryConditions);

    /**
     * 主数据维度-导入数据成功数量、失败数量、新增数量、编辑数量
     *
     * @param tableName
     * @param queryConditions
     * @return
     */
    String buildExportDataCount(String tableName, String queryConditions);

    /**
     * 查询一条数据
     *
     * @param tableName
     * @param queryConditions
     * @return
     */
    String buildQueryData(String tableName, String queryConditions);

    /**
     * 查询mdm表code和name数据
     *
     * @param tableName
     * @param code
     * @param name
     * @param versionId
     * @return
     */
    String buildQueryCodeAndName(String tableName, String code, String name, Integer versionId);

    /**
     * 查询mdm表code和lock数据
     */
    String buildQueryCodeAndLock(String tableName, String code,Integer versionId);

    /**
     * 获取主数据列表筛选条件运算符
     *
     * @return
     */
    List<OperatorVO> getOperatorList();

    /**
     * 拼接运算符where条件
     *
     * @param operators
     * @return
     */
    String buildOperatorCondition(List<FilterQueryDTO> operators);

    /**
     * 生成单条insert语句
     *
     * @param data
     * @param tableName
     * @return
     */
    String buildInsertSingleData(Map<String, Object> data, String tableName);

}
