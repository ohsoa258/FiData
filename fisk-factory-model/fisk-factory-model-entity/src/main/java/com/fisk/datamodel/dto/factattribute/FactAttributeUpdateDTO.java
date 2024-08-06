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

    /**
     * 配置详情(维度key的json配置详情)
     */
    @ApiModelProperty(value = "配置详情(维度key的json配置详情)")
    public String configDetails;

    /**
     * 数据分类：DataClassificationEnum
     * PUBLIC_DATA(1, "公开数据", "green"),
     * INTERNAL_DATA(2, "内部数据", "blue"),
     * MAX(3, "敏感数据", "orange"),
     * MIN(4, "高度敏感数据", "red"),
     */
    @ApiModelProperty(value = "数据分类：DataClassificationEnum")
    public Integer dataClassification;

    /**
     * 数据分级：DataLevelEnum
     * LEVEL1(1, "一级（一般数据）", "green"),
     * LEVEL2(2, "二级（重要数据）", "blue"),
     * LEVEL3(3, "三级（敏感数据）", "orange"),
     * LEVEL4(4, "四级（核心数据）", "red"),
     */
    @ApiModelProperty(value = "数据分级：DataLevelEnum")
    public Integer dataLevel;

    /**
     * 属性类型：0:退化维度，1:维度键，2:度量字段
     */
    @ApiModelProperty(value = "属性类型：0:退化维度，1:维度键，2:度量字段")
    public int attributeType;

}
