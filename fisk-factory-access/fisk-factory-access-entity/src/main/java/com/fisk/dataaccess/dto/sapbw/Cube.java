package com.fisk.dataaccess.dto.sapbw;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lsj
 * @description sapbw的对象，装载单个cube的所有详情
 * @date 2022/5/27 15:36
 */
@Data
public class Cube {

    /**
     * cube的cat名称 CAT_NAM
     */
    @ApiModelProperty(value = "cube的cat名称")
    public String catName;

    /**
     * cube的名称 CUBE_NAM
     */
    @ApiModelProperty(value = "cube的名称")
    public String cubeName;

    /**
     * cube的类型 CUBE_TYPE
     */
    @ApiModelProperty(value = "cube的类型")
    public String cubeType;

    /**
     * cube的UID CUBE_UID
     */
    @ApiModelProperty(value = "cube的UID")
    public String cubeUid;

    /**
     * cube的创建时间 CREATED_ON
     */
    @ApiModelProperty(value = "cube的创建时间")
    public String cubeCreatedOn;

    /**
     * cube结构最近的修改时间 LST_SCHEMA_UPD
     */
    @ApiModelProperty(value = "cube结构最近的修改时间")
    public String cubeLstSchemaUpd;

    /**
     * cube结构最近的修改人 SCHEMA_UPD_BY
     */
    @ApiModelProperty(value = "cube结构最近的修改人")
    public String cubeSchemaUpdBy;

    /**
     * cube数据最近的修改时间 LST_DATA_UPD
     */
    @ApiModelProperty(value = "cube数据最近的修改时间")
    public String cubeLstDataUpd;

    /**
     * cube数据最近的修改人 DATA_UPD_BY DSCRPTN
     */
    @ApiModelProperty(value = "cube数据最近的修改人")
    public String cubeDataUpdBy;

    /**
     * cube的描述 DSCRPTN
     */
    @ApiModelProperty(value = "cube数据最近的修改人")
    public String cubeDscrptn;

}
