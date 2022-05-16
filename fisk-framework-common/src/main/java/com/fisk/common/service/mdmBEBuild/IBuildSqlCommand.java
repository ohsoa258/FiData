package com.fisk.common.service.mdmBEBuild;

import com.fisk.common.service.mdmBEBuild.dto.InsertImportDataDTO;
import com.fisk.common.service.mdmBEBuild.dto.PageDataDTO;

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
    String buildPageData(PageDataDTO dto);

}
