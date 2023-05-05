package com.fisk.dataaccess.dto.oraclecdc;

import com.fisk.dataaccess.dto.table.FieldNameDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class CdcJobParameterDTO {

    @ApiModelProperty(value = "应用id")
    public Integer appId;
    /**
     * 目标表id
     */
    @ApiModelProperty(value = "目标表id")
    public Integer tableAccessId;

    /**
     * 检查点id
     */
    @ApiModelProperty(value = "检查点id")
    public Integer savepointHistoryId = 0;
    /**
     * 表字段
     */
    @ApiModelProperty(value = "表字段")
    public List<FieldNameDTO> fieldNameDTOList;

}
