package com.fisk.mdm.vo.attributeGroup;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.common.core.baseObject.vo.BaseUserInfoVO;
import com.fisk.mdm.dto.attributeGroup.AttributeGroupDetailsDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author WangYan
 * @Date 2022/5/23 8:38
 * @Version 1.0
 */
@Data
public class AttributeGroupVO extends BaseUserInfoVO {

    private Integer id;
    private Integer modelId;
    private String name;
    private String details;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间",required = true)
    public LocalDateTime createTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间",required = true)
    public LocalDateTime updateTime;

    private List<AttributeGroupDetailsDTO> groupDetailsList;
}
