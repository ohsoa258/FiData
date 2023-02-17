package com.fisk.task.dto.daconfig;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.task.SynchronousTypeEnum;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class OverLoadCodeDTO {

    public DataAccessConfigDTO config;

    public SynchronousTypeEnum synchronousTypeEnum;

    public String funcName;

    public BuildNifiFlowDTO buildNifiFlow;

    public DataSourceTypeEnum dataSourceType;

}
