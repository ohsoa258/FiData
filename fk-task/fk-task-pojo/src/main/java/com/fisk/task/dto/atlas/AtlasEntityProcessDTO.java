package com.fisk.task.dto.atlas;

import fk.atlas.api.model.EntityProcess;

import java.util.List;

/**
 * @author: DennyHui
 * CreateTime: 2021/7/15 11:42
 * Description:
 */
public class AtlasEntityProcessDTO {
    public String createUser;
    public String processName;
    public String higherType;
    public List<EntityProcess.entity> inputs;
    public List<EntityProcess.entity> outputs;
    public String des;
    public String qualifiedName;
}
