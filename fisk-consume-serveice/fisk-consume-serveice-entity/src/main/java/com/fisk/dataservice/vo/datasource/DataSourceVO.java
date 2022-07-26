package com.fisk.dataservice.vo.datasource;

import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description 数据源
 * @date 2022/1/14 18:27
 */
@Data
public class DataSourceVO
{
    @ApiModelProperty(value = "数据源信息")
    public  DataSourceConVO dataSourceCon;

    @ApiModelProperty(value = "树节点信息")
    public FiDataMetaDataTreeDTO tree;
}
