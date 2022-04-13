package com.fisk.datagovernance.vo.dataquality.datasource;

import com.fisk.common.service.dbMetaData.dto.TablePyhNameDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据库
 * @date 2022/4/13 11:23
 */
@Data
public class DataBaseSourceVO {
    /**
     * 数据库名称
     */
    @ApiModelProperty(value = "数据库名称")
    public String conDbname;

    /**
     * 表
     */
    @ApiModelProperty(value = "表")
    public List<TablePyhNameDTO> children;
}
