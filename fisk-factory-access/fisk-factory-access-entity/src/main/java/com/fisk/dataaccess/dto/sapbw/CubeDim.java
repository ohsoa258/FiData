package com.fisk.dataaccess.dto.sapbw;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lsj
 * @description sapbw的对象，装载cube的单个维度的详情
 * @date 2022/5/27 15:36
 */
@Data
public class CubeDim {

    /**
     * 该维度的cube的cat名称 CAT_NAM
     */
    @ApiModelProperty(value = "该变量来源的cube的cat名称")
    public String dimCatName;

    /**
     * 该维度的cube的cat名称 CUBE_NAM
     */
    @ApiModelProperty(value = "该变量来源的cube的cubeName名称")
    public String dimCubeName;

    /**
     * 维度名称 DIM_NAM
     */
    @ApiModelProperty(value = "维度名称")
    public String dimName;

    /**
     * 维的唯一名称 DIM_UNAM
     */
    @ApiModelProperty(value = "维的唯一名称")
    public String dimUname;

    /**
     * 维度UID DIM_UID
     */
    @ApiModelProperty(value = "维度UID")
    public String dimUid;

    /**
     * 维度的描述 DIM_CAP
     */
    @ApiModelProperty(value = "维度的描述")
    public String dimCap;

    /**
     * 当前cube下维度的排序序号 DIM_ORDINAL
     */
    @ApiModelProperty(value = "维度的排序序号")
    public String dimOrdinal;

    /**
     * 维度的类型 DIM_TYPE
     */
    @ApiModelProperty(value = "维度的类型")
    public String dimType;

    /**
     * 维度的数组基数(大致成员数) DIM_CARDINALITY
     */
    @ApiModelProperty(value = "维度的数组基数(大致成员数)")
    public String dimCardinality;

    /**
     * 维度的层次结构的唯一名称 DFLT_HRY
     */
    @ApiModelProperty(value = "维度的层次结构的唯一名称")
    public String dimDfltHry;

    /**
     * 维度的描述 DSCRPTN
     */
    @ApiModelProperty(value = "维度的描述")
    public String dimDescribe;

}
