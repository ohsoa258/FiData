package com.fisk.chartvisual.dto;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author JinXingWang
 */
public class SlicerQuerySsasObject {
    @NotNull
    public Integer id;
    @NotNull
    public String hierarchyName;
}
