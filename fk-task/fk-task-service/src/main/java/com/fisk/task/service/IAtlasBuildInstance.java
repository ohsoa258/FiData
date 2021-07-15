package com.fisk.task.service;

import com.fisk.common.entity.BusinessResult;
import com.fisk.task.dto.atlas.AtlasEntityDeleteDTO;
import com.fisk.task.dto.atlas.AtlasEntityProcessDTO;
import fk.atlas.api.model.*;

/**
 * @author: DennyHui
 * CreateTime: 2021/7/8 12:08
 * Description:
 */
public interface IAtlasBuildInstance {
    BusinessResult atlasBuildInstance(EnttityRdbmsInstance.entity_rdbms_instance data);
    BusinessResult atlasBuildDb(EntityRdbmsDB.entity_rdbms_db data);
    BusinessResult atlasBuildProcess(EntityProcess.entity_rdbms_process data);
    BusinessResult atlasBuildTable(EntityRdbmsTable.entity_rdbms_table data);
    BusinessResult atlasBuildTableColumn(EntityRdbmsColumn.entity_rdbms_column data);
    BusinessResult atlasEntityDelete(AtlasEntityDeleteDTO data);
    BusinessResult atlasBuildProcess(AtlasEntityProcessDTO data);
}
