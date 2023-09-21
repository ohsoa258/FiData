package com.fisk.dataaccess.dto.sapbw;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lsj
 * @description sapbw的对象，装载cube的单个变量的详情
 * @date 2022/5/27 15:36
 */
@Data
public class CubeVariable {

    /**
     * 该变量来源的cube的cat名称 CAT_NAM
     */
    @ApiModelProperty(value = "该变量来源的cube的cat名称")
    public String catName;

    /**
     * 该变量来源的cube的cat名称 CAT_NAM
     */
    @ApiModelProperty(value = "该变量来源的cube的cubeName名称")
    public String cubeName;

    /**
     * 变量名称 VAR_NAM
     */
    @ApiModelProperty(value = "变量名称")
    public String varName;

    /**
     * 变量描述 VAR_CAP  VAR_UID
     */
    @ApiModelProperty(value = "变量描述")
    public String varCap;

    /**
     * 变量UID VAR_UID
     */
    @ApiModelProperty(value = "变量UID")
    public String varUid;

    /**
     * 变量排序序号 VAR_ORDINAL
     */
    @ApiModelProperty(value = "变量排序")
    public String varOrder;

    /**
     * 数据类型 VAR_TYPE
     */
    @ApiModelProperty(value = "数据类型")
    public String varDataType;

    /**
     * 以字符或字节表示的值最大长度 CHR_MAX_LEN
     */
    @ApiModelProperty(value = "以字符或字节表示的值最大长度")
    public String varChrMaxLen;

    /**
     * 变量关联的维度名 REF_DIM
     */
    @ApiModelProperty(value = "变量关联的维度名")
    public String varRefDim;

    /**
     * REF_HRY
     * 官方描述:层次结构的唯一名称
     */
    @ApiModelProperty(value = "层次结构的唯一名称")
    public String varRefHry;

    /**
     * 变量已设置的小参数值 DFLT_LOW
     * 官方描述：成员唯一名称
     */
    @ApiModelProperty(value = "变量已设置的参数值")
    public String varDfltLow;

    /**
     * 变量已设置的大参数值 DFLT_HIGH   DFLT_LOW和DFLT_HIGH可以搭配使用 表示已设置参数值的区间
     * 官方描述：成员唯一名称
     */
    @ApiModelProperty(value = "变量已设置的大参数值")
    public String varDfltHigh;

    /**
     * 变量已设置的小参数值的描述 DFLT_LOW_CAP
     */
    @ApiModelProperty(value = "变量已设置的小参数值的描述")
    public String varDfltLowCap;

    /**
     * 变量已设置的大参数值的描述 DFLT_HIGH_CAP
     */
    @ApiModelProperty(value = "变量已设置的大参数值的描述")
    public String varDfltHighCap;

    /**
     * 变量的描述 DSCRPTN
     */
    @ApiModelProperty(value = "变量已设置的大参数值的描述")
    public String varDescribe;

}
