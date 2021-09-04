package com.fisk.common.mdc;

import com.fisk.common.enums.BaseEnum;

/**
 * MDC类型
 *
 * @author gy
 */
public enum TraceTypeEnum implements BaseEnum {

    /**
     *
     */
    PROJECT_START(1, "Project_Start"),
    PROJECT_SHUTDOWN(2, "Project_Shutdown"),
    CHARTVISUAL_QUERY(1000, "ChartVisual_Query"),
    CHARTVISUAL_CONNECTION(1001, "ChartVisual_Connection"),
    CHARTVISUAL_SERVICE(1002, "ChartVisual_Service"),
    TASK_MQ_PRODUCER_CONFIRM(2001, "Task_MQ_Producer_Confirm"),
    TASK_WS_SEND_MESSAGE(2002, "Task_WS_Send_Message"),
    TASK_NIFI_ERROR(2003, "Task_Nifi_Error"),
    UNKNOWN(-1, "UNKNOWN"),
    ATLASINSTANCE_MQ_BUILD(2004,"AtlasInstance_MQ_Build"),
    ATLASTABLECOLUMN_MQ_BUILD(2005,"AtlasTableColumn_MQ_Build"),
    ATLASENTITYDELETE_MQ_BUILD(2006,"AtlasEntityDelete_MQ_Build"),
    DORIS_MQ_BUILD(2007,"Doris_MQ_Build"),
    DORIS_INCREMENTAL_MQ_BUILD(2007,"Doris_Incremental_MQ_Build"),
    DATAMODEL_DORIS_TABLE_MQ_BUILD(2008,"DataModel_Doris_Table_MQ_Build"),
    DATAACCESS_GET_ATLAS_BUILDTABLE_AND_COLUMN(3001,"GetAtlasBuildTableAndColumn_Error"),
    DATAACCESS_GET_ATLAS_ENTITY(3002,"GetAtlasEntity_Error"),
    DATAACCESS_GET_ATLAS_WRITEBACKDATA(3003,"GetAtlasWriteBackData_Error"),
    DATAACCESS_CONFIG(3004,"DataAccessConfig_Error"),
    DATAINPUT_PG_TABLE_BUILD(3005,"DataInputBuildPGTable"),
    DATAINPUT_PG_STGTOODS_BUILD(3006,"DataInputBuildPGStgToOds"),
    OLAP_CREATEMODEL_BUILD(3007,"OlapCreateModel");
    TraceTypeEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    private final String name;
    private final int value;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

}
