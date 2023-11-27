package com.fisk.common.service.factorymdmdomainscript;


import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceRelationsDTO;

import java.util.List;

/**
 * @author wangjian
 */
public interface IBuildMdmScript {

    /**
     * 拼接主数据构建基于域脚本
     *
     * @return
     */
    String buildMdmScript(List<TableSourceRelationsDTO> dto);
}
