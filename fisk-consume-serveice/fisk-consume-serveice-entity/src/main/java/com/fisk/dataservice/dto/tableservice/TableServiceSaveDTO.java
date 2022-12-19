package com.fisk.dataservice.dto.tableservice;

import com.fisk.dataservice.dto.tablefields.TableFieldDTO;
import com.fisk.dataservice.dto.tablesyncmode.TableSyncModeDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class TableServiceSaveDTO {

    public TableServiceDTO tableService;

    public List<TableFieldDTO> tableFieldList;

    public TableSyncModeDTO tableSyncMode;

}
