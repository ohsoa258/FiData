package com.fisk.common.service.metadata.dto.metadata;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 * @date 2022-07-01 14:20
 */
@Data
public class  MetaDataInstanceAttributeDTO extends MetaDataBaseAttributeDTO {
    @ApiModelProperty(value = "创建实例：数据库类型")
    public String rdbms_type;
    @ApiModelProperty(value = "创建实例：平台")
    public String platform;
    @ApiModelProperty(value = "创建实例：主机名")
    public String hostname;
    @ApiModelProperty(value = "创建实例：端口")
    public String port;
    @ApiModelProperty(value = "创建实例：网络协议")
    public String protocol;
    @ApiModelProperty(value = "平台配置数据源名称")
    public String sourceName;
    @ApiModelProperty(value = "数据库集合")
    public List<MetaDataDbAttributeDTO> dbList;
}
