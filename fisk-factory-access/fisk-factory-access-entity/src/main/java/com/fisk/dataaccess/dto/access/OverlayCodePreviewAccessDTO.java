package com.fisk.dataaccess.dto.access;

import com.fisk.dataaccess.dto.table.TableBusinessDTO;
import com.fisk.dataaccess.dto.table.TableFieldsDTO;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
public class OverlayCodePreviewAccessDTO {

    public List<TableFieldsDTO> modelPublishFieldDTOList;

    public Integer syncMode;

    public Integer id;

    public TableBusinessDTO tableBusiness;

}
