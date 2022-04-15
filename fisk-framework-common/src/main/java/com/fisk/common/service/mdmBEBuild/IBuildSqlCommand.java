package com.fisk.common.service.mdmBEBuild;

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
}
