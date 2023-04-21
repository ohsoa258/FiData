package com.fisk.datamanagement.dto.assetsdirectory;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 * @date 2022-06-20 17:56
 */
@Data
public class AssetsDirectoryDTO {

    @ApiModelProperty(value = "key")
    public String key;

    @ApiModelProperty(value = "parent")
    public String parent;

    @ApiModelProperty(value = "名称")
    public String name;

    @ApiModelProperty(value = "水平")
    public Integer level;

    @ApiModelProperty(value = "参数skip")
    public Boolean skip;

    @ApiModelProperty(value = "超级类型")
    public List<String> superTypes;


}
