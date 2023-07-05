package com.fisk.common.service.factorymodelkeyscript;


import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceRelationsDTO;
import com.fisk.common.service.factorycodepreview.factorycodepreviewdto.PublishFieldDTO;

import java.util.List;

/**
 * @author lishiji
 */
public interface IBuildFactoryModelKeyScript {

    /**
     * 拼接数仓建模构建维度key脚本
     *
     * @return
     */
    String buildKeyScript(List<TableSourceRelationsDTO> dto);
}
