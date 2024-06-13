package com.fisk.datamanagement.dto.metamap;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MetaMapTblDTO {

    /**
     * 表id
     */
    @ApiModelProperty(value = "表id")
    private Integer tblId;

    /**
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    private String tblName;

    /**
     * 表显示名称
     */
    @ApiModelProperty(value = "表显示名称")
    private String displayName;

    /**
     * 创建人id
     */
    @ApiModelProperty(value = "创建人id")
    private String createUser;

    /**
     * 创建人姓名
     */
    @ApiModelProperty(value = "创建人姓名")
    private String createUserName;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

}
