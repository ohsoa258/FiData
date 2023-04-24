package com.fisk.datamodel.dto.factattribute;

import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class FactAttributeUpdateDTO {
    @ApiModelProperty(value = "id")
    public int id;
    @ApiModelProperty(value = "事实字段中文名")
    public String factFieldCnName;
    @ApiModelProperty(value = "事实字段类型")
    public String factFieldType;
    @ApiModelProperty(value = "事实字段长度")
    public int factFieldLength;
    @ApiModelProperty(value = "事实字段详细信息")
    public String factFieldDes;
    @ApiModelProperty(value = "事实字段英文名")
    public String factFieldEnName;
    /**
     * 接入的增量时间参数
     */
    @ApiModelProperty(value = "接入的增量时间参数")
    public List<DeltaTimeDTO> deltaTimes;
    /**
     * 预览nifi调用SQL执行语句
     */
    @ApiModelProperty(value = "预览nifi调用SQL执行语句")
    public String execSql;
}
