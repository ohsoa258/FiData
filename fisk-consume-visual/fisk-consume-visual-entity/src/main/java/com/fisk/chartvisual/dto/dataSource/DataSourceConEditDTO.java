package com.fisk.chartvisual.dto.dataSource;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * 数据源连接 编辑DTO
 * @author gy
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DataSourceConEditDTO extends DataSourceConDTO{

    @NotNull(message = "id不可为null")
    public Integer id;
}
