package com.fisk.task.utils.mdmBEBuild;

import com.fisk.mdm.vo.entity.EntityVO;

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
     * @param entityVo
     * @return
     */
    String buildStgTable(EntityVO entityVo);

    /**
     * 创建表mdm
     * @param entityVo
     * @return
     */
    String buildMdmTable(EntityVO entityVo);

    /**
     * 创建视图view
     * @param entityVo
     * @return
     */
    String buildViewTable(EntityVO entityVo);
}
