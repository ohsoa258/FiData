package com.fisk.dataaccess.dto.app;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Objects;

/**
 * <p>
 *     应用注册添加判断名称(简称)是否重复
 * </p>
 * @author Lock
 */
@Data
public class RepeatNameDTO {
    /**
     * 应用名称
     */
    @ApiModelProperty(value = "应用名称",required = true)
    public String appName;

    /**
     * 应用简称
     */
    @ApiModelProperty(value = "应用简称",required = true)
    public String appAbbreviation;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RepeatNameDTO that = (RepeatNameDTO) o;
        return Objects.equals(appName, that.appName) && Objects.equals(appAbbreviation, that.appAbbreviation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appName, appAbbreviation);
    }
}
