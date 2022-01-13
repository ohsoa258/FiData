package com.fisk.dataservice.dto.datasource;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version v1.0
 * @description
 * @date 2022/1/6 14:51
 */
public class SlicerQuerySsasObject {
    @NotNull
    public Integer id;
    @NotNull
    public String hierarchyName;
}
