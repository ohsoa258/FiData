package com.fisk.dataservice.dto.tableservice;

import com.fisk.dataservice.dto.tablefields.TableFieldDTO;
import com.fisk.dataservice.dto.tablesyncmode.TableSyncModeDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class TableServiceSaveDTO {

    @ApiModelProperty(value = "表服务")
    public TableServiceDTO tableService;

    @ApiModelProperty(value = "表应字段列表")
    public List<TableFieldDTO> tableFieldList;

    @ApiModelProperty(value = "表同步方式", required = true)
    public TableSyncModeDTO tableSyncMode;

}
