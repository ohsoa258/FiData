package com.fisk.common.service.mdmBEBuild;

import com.fisk.common.service.mdmBEBuild.dto.ImportDataPageDTO;
import com.fisk.common.service.mdmBEBuild.dto.InsertImportDataDTO;
import com.fisk.common.service.mdmBEBuild.dto.MasterDataPageDTO;

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
     * @param importType
     * @return
     */
    String buildUpdateImportData(Map<String, Object> jsonObject, String tableName, int importType);

    /**
     * 主数据维护-首页列表
     *
     * @param dto
     * @return
     */
    String buildMasterDataPage(MasterDataPageDTO dto);

}
