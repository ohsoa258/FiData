package com.fisk.dataaccess.dto.access;

import com.fisk.dataaccess.dto.factorycodepreviewdto.AccessFullVolumeSnapshotDTO;
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

    @ApiModelProperty(value = "发布模式字段DTO列表")
    public List<TableFieldsDTO> modelPublishFieldDTOList;

    @ApiModelProperty(value = "同步模式")
    public Integer syncMode;

    @ApiModelProperty(value = "id")
    public Integer id;

    @ApiModelProperty(value = "表业务")
    public TableBusinessDTO tableBusiness;

    @ApiModelProperty(value = "全量覆盖方式版本快照对象")
    public AccessFullVolumeSnapshotDTO snapshotDTO;

}
