package com.fisk.dataservice.dto.dataanalysisview;

import com.fisk.common.service.dbMetaData.dto.TableNameDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/

@Data
public class DataSourceViewDTO {

    @ApiModelProperty(value = "id")
    public long id;
    /**
     * 数据源
     */
    @ApiModelProperty(value = "数据源")
    public String driveType;
    /**
     * 应用名称
     */
    @ApiModelProperty(value = "应用名称")
    public String appName;
    /**
     * 表
     */
    @ApiModelProperty(value = "表列表")
    public List<TableNameDTO> tableDtoList;

    /**
     * 视图
     */
//    public List<DataBaseViewDTO> viewDtoList;
}
