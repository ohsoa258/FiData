package com.fisk.datagovernance.vo.dataops;

import com.fisk.common.service.dbMetaData.dto.TablePyhNameDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 表信息
 * @date 2022/4/22 21:48
 */
@Data
public class DataOpsDataBaseVO {
    /**
     * 数据源id
     */
    @ApiModelProperty(value = "数据源id")
    public int id;

    /**
     * 数据库名称
     */
    @ApiModelProperty(value = "数据库名称")
    public String conDbname;

    /**
     * 表
     */
    @ApiModelProperty(value = "表")
    public List<String> children;
}
