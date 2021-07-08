package com.fisk.task.dto.atlas;

import com.fisk.task.dto.MQBaseDTO;
import fk.atlas.api.model.*;
import lombok.Data;

/**
 * @author: DennyHui
 * CreateTime: 2021/7/7 18:18
 * Description:
 */
@Data
public class AtlasEntityRdbmsDTO extends MQBaseDTO {
   public EnttityRdbmsInstance.entity_rdbms_instance entityInstance;
   public EntityRdbmsDB.entity_rdbms_db entityDb;
   public EntityRdbmsTable.entity_rdbms_table entityTable;
   public EntityRdbmsColumn.entity_rdbms_column entityTableColumn;
   public EntityProcess.entity_rdbms_process entityProcess;
}
