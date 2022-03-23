package com.fisk.dataaccess.vo.datareview;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Lock
 */
@Data
public class DataReviewVO {

    /**
     * id
     */
    @ApiModelProperty(value = "应用id", required = true)
    public long id;
    /**
     * 应用名称
     */
    @ApiModelProperty(value = "物理表id", required = true)
    public long tableAccessId;
    /**
     * 字段名称
     */
    @ApiModelProperty(value = "字段名称", required = true)
    public String fieldName;
    /**
     * 字段描述
     */
    @ApiModelProperty(value = "字段描述", required = true)
    public String fieldDes;
    /**
     * 字段类型
     */
    @ApiModelProperty(value = "字段类型", required = true)
    public String fieldType;
    /**
     * 字段长度
     */
    @ApiModelProperty(value = "字段长度", required = true)
    public String fieldLength;
    /**
     * 是否主键
     */
    @ApiModelProperty(value = "是否主键", required = true)
    public int isPrimarykey;
    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    public LocalDateTime createTime;

}
