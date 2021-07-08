package com.fisk.task.service.impl;

import com.fisk.common.entity.BusinessResult;
import com.fisk.task.service.IAtlasBuildInstance;
import com.fisk.task.utils.YamlReader;
import fk.atlas.api.AtlasClient;
import fk.atlas.api.model.EntityProcess;
import fk.atlas.api.model.EntityRdbmsDB;
import fk.atlas.api.model.EnttityRdbmsInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

/**
 * @author: DennyHui
 * CreateTime: 2021/7/8 12:09
 * Description:
 */
@Service
@Slf4j
public class AtlasBuildInstance implements IAtlasBuildInstance {

    private String atlas_url = YamlReader.instance.getValueByKey("atlasconstr.url").toString();
    private String atlas_username = YamlReader.instance.getValueByKey("atlasconstr.username").toString();
    private String atlas_pwd = YamlReader.instance.getValueByKey("atlasconstr.password").toString();
    private AtlasClient ac = new AtlasClient(atlas_url, atlas_username, atlas_pwd);
    @Resource
    IAtlasBuildInstance atlas;

    @Override
    public BusinessResult atlasBuildInstance(EnttityRdbmsInstance.entity_rdbms_instance data){
        BusinessResult resInstance;
        try {
            ac.CreateEntity_rdbms_instance(data);
            resInstance=new BusinessResult(true,"atlas instance 创建成功");
        } catch (Exception e) {
            resInstance=new BusinessResult(false,e.getMessage());
            log.error(e.getMessage());
        }
        return resInstance;
    }
    @Override
    public BusinessResult atlasBuildDb(EntityRdbmsDB.entity_rdbms_db data){
        BusinessResult resDB;
        try {
            ac.CreateEntity_rdbms_db(data);
            resDB=new BusinessResult(true,"atlas DB 创建成功");
        } catch (Exception e) {
            resDB=new BusinessResult(false,e.getMessage());
            log.error(e.getMessage());
        }
        return resDB;
    }
    @Override
    public BusinessResult atlasBuildProcess(EntityProcess.entity_rdbms_process data)
    {
        BusinessResult resProcess;
        try {
            ac.CreateEntityProcess(data);
            resProcess=new BusinessResult(true,"atlas Process 创建成功");
        } catch (Exception e) {
            resProcess=new BusinessResult(false,e.getMessage());
            log.error(e.getMessage());
        }
        return resProcess;
    }
}
