package com.fisk.datamodel.dto.businessarea;

import com.fisk.dataaccess.dto.table.TableBusinessDTO;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class OverlayCodePreviewDTO {

   public List<ModelPublishFieldDTO> modelPublishFieldDTOList;

   public String targetTableName;

   public Integer syncMode;

   public TableBusinessDTO tableBusiness;


}
