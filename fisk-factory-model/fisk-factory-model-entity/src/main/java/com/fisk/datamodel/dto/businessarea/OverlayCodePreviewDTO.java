package com.fisk.datamodel.dto.businessarea;

import com.fisk.dataaccess.dto.table.TableBusinessDTO;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class OverlayCodePreviewDTO {

   @ApiModelProperty(value = "模型发布字段DTO列表")
   public List<ModelPublishFieldDTO> modelPublishFieldDTOList;

   /**
    * 同步方式
    */
   @ApiModelProperty(value = "同步模式")
   public Integer syncMode;

   /**
    * 表业务
    */
   @ApiModelProperty(value = "表业务")
   public TableBusinessDTO tableBusiness;

   /**
    * 更新sql
    */
   @ApiModelProperty(value = "updateSql")
   public String updateSql;

   @ApiModelProperty(value = "id")
   public Integer id;

   /**
    * 0 维度 1 事实
    * 李世纪2023-04-24修改：  1、维度  2、事实
    */
   @ApiModelProperty(value = "1 维度 2 事实")
   public Integer type;

}
