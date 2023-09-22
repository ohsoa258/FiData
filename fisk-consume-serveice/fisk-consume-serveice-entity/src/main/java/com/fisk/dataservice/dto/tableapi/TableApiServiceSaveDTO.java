package com.fisk.dataservice.dto.tableapi;
import com.fisk.dataservice.dto.tablesyncmode.TableSyncModeDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class TableApiServiceSaveDTO {

    @ApiModelProperty(value = "表服务")
    public TableApiServiceDTO tableApiServiceDTO;

    @ApiModelProperty(value = "表服务参数")
    public List<TableApiParameterDTO> tableApiParameterDTO;

    @ApiModelProperty(value = "表同步方式", required = true)
    public TableSyncModeDTO tableSyncMode;
}
