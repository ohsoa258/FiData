package com.fisk.task.service.impl;

import com.fisk.common.entity.BusinessResult;
import com.fisk.task.dto.atlas.AtlasEntityDeleteDTO;
import com.fisk.task.service.IAtlasBuildInstance;
import com.fisk.task.utils.YamlReader;
import fk.atlas.api.AtlasClient;
import fk.atlas.api.model.*;
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
    /**
     * Atlas 创建实例
     * @param data
     * @return
     */
    @Override
    public BusinessResult atlasBuildInstance(EnttityRdbmsInstance.entity_rdbms_instance data) {
        BusinessResult resInstance;
        try {
            String res = ac.CreateEntity_rdbms_instance(data);
            res = res.substring(res.lastIndexOf(":") + 2, res.lastIndexOf("\""));
            resInstance = BusinessResult.of(true, "atlas instance 创建成功", res);
        } catch (Exception e) {
            resInstance = new BusinessResult(false, e.getMessage());
            log.error(e.getMessage());
        }
        return resInstance;
    }
    /**
     * Atlas 创建DB
     * @param data
     * @return
     */
    @Override
    public BusinessResult atlasBuildDb(EntityRdbmsDB.entity_rdbms_db data) {
        BusinessResult resDB;
        try {
            String res = ac.CreateEntity_rdbms_db(data);
            res = res.substring(res.lastIndexOf(":") + 2, res.lastIndexOf("\""));
            resDB = BusinessResult.of(true,"atlas db 创建成功",res);
        } catch (Exception e) {
            resDB = new BusinessResult(false, e.getMessage());
            log.error(e.getMessage());
        }
        return resDB;
    }
    /**
     * Atlas 创建实体连接
     * @param data
     * @return
     */
    @Override
    public BusinessResult atlasBuildProcess(EntityProcess.entity_rdbms_process data) {
        BusinessResult resProcess;
        try {
            resProcess = new BusinessResult(true, ac.CreateEntityProcess(data));
        } catch (Exception e) {
            resProcess = new BusinessResult(false, e.getMessage());
            log.error(e.getMessage());
        }
        return resProcess;
    }

    /**
     * Atlas 创建表
     * @param data
     * @return
     */
    @Override
    public BusinessResult atlasBuildTable(EntityRdbmsTable.entity_rdbms_table data){
        BusinessResult resTb;
        try {
            String res = ac.CreateEntity_rdbms_table(data);
            res = res.substring(res.lastIndexOf(":") + 2, res.lastIndexOf("\""));
            resTb = BusinessResult.of(true,"atlas table 创建成功",res);
        } catch (Exception e) {
            resTb = new BusinessResult(false, e.getMessage());
            log.error(e.getMessage());
        }
        return resTb;
    }

    /**
     * atlas 创建表字段
     * @param data
     * @return
     */
    @Override
    public BusinessResult atlasBuildTableColumn(EntityRdbmsColumn.entity_rdbms_column data){
        BusinessResult resCl;
        try {
            String res = ac.CreateEntity_rdbms_table_column(data);
            res = res.substring(res.lastIndexOf(":") + 2, res.lastIndexOf("\""));
            resCl = BusinessResult.of(true,"atlas table 创建成功",res);
        } catch (Exception e) {
            resCl = new BusinessResult(false, e.getMessage());
            log.error(e.getMessage());
        }
        return resCl;
    }

    /**
     * atlas 删除实体
     * @param data
     * @return
     */
    @Override
    public BusinessResult atlasEntityDelete(AtlasEntityDeleteDTO data){
        BusinessResult resDel;
        try{
            String res = ac.DeleteEntity(data.entityId);
            resDel =new BusinessResult(true,"atlas entity 删除成功");
        }catch(Exception e){
            resDel = new BusinessResult(false, e.getMessage());
            log.error(e.getMessage());
        }
        return  resDel;
    }
}
