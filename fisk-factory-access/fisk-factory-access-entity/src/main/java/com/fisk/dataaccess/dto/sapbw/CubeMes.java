package com.fisk.dataaccess.dto.sapbw;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lsj
 * @description sapbw的对象，装载cube的单个指标的详情
 * @date 2022/5/27 15:36
 */
@Data
public class CubeMes {

    /**
     * 该指标的cube的cat名称 CAT_NAM
     */
    @ApiModelProperty(value = "该变量来源的cube的cat名称")
    public String mesCatName;

    /**
     * 该指标的cube的cat名称 CUBE_NAM
     */
    @ApiModelProperty(value = "该变量来源的cube的cubeName名称")
    public String mesCubeName;

    /**
     * 指标名称 MES_NAM
     */
    @ApiModelProperty(value = "指标名称")
    public String mesName;

    /**
     * 维的唯一名称 MES_UNAM
     */
    @ApiModelProperty(value = "维的唯一名称")
    public String mesUname;

    /**
     * 指标的描述 MES_CAP
     */
    @ApiModelProperty(value = "指标的描述")
    public String mesCap;

    /**
     * 指标UID MES_UID
     */
    @ApiModelProperty(value = "指标UID")
    public String mesUid;

    /**
     * 指标关键值的集合特点 MES_AGGREGATOR
     */
    @ApiModelProperty(value = "指标关键值的集合特点")
    public String mesAggregator;

    /**
     * 指标的数据类型 DATA_TYPE
     */
    @ApiModelProperty(value = "指标的数据类型")
    public String mesDataType;

    /**
     * 指标的最大精度(仅当数据类型为数字时) NUM_PREC
     */
    @ApiModelProperty(value = "指标的最大精度(仅当数据类型为数字时)")
    public String mesNumPrec;

    /**
     * 指标的显示因子(如果可用) NUM_SCALE MES_UNITS
     */
    @ApiModelProperty(value = "指标的显示因子(如果可用)")
    public String mesNumScale;

    /**
     * 指标的计量单位(美元, 项目, 如可用) MES_UNITS
     */
    @ApiModelProperty(value = "指标的计量单位(美元, 项目, 如可用)")
    public String mesUnits;

    /**
     * 指标的描述 DSCRPTN
     */
    @ApiModelProperty(value = "变量已设置的大参数值的描述")
    public String mesDescribe;

}
