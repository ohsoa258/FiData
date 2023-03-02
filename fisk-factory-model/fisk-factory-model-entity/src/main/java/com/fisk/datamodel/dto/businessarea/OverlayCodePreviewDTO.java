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

   public Integer syncMode;

   public TableBusinessDTO tableBusiness;

   public String updateSql;

   public Integer id;

   /**
    * 0 维度 1 事实
    */
   public Integer type;


}
