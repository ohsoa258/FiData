package com.fisk.task.service;

import com.fisk.common.entity.BusinessResult;
import fk.atlas.api.model.EntityProcess;
import fk.atlas.api.model.EntityRdbmsDB;
import fk.atlas.api.model.EnttityRdbmsInstance;

/**
 * @author: DennyHui
 * CreateTime: 2021/7/8 12:08
 * Description:
 */
public interface IAtlasBuildInstance {
    BusinessResult atlasBuildInstance(EnttityRdbmsInstance.entity_rdbms_instance data);
    BusinessResult atlasBuildDb(EntityRdbmsDB.entity_rdbms_db data);
    BusinessResult atlasBuildProcess(EntityProcess.entity_rdbms_process data);
}
