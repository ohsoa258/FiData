package com.fisk.task.utils.mdmBEBuild;

import com.fisk.mdm.vo.entity.EntityInfoVO;

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
     * @return
     */
    String buildStgTable(EntityInfoVO entityInfoVo);

    /**
     * 创建表mdm
     * @param entityInfoVo
     * @return
     */
    String buildMdmTable(EntityInfoVO entityInfoVo);

    /**
     * 创建视图view
     * @param entityInfoVo
     * @return
     */
    String buildViewTable(EntityInfoVO entityInfoVo);
}
