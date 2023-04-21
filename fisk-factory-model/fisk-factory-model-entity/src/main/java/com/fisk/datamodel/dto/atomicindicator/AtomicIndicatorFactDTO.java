package com.fisk.datamodel.dto.atomicindicator;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class AtomicIndicatorFactDTO {
    @ApiModelProperty(value = "事实Id")
    public long factId;

    @ApiModelProperty(value = "事实表")
    public String factTable;

    @ApiModelProperty(value = "业务区域Id")
    public int businessAreaId;

    @ApiModelProperty(value = "列表")
    public List<AtomicIndicatorPushDTO> list;
    /**
     * 拼接外部表
     */
    @ApiModelProperty(value = "拼接外部表")
    public List<AtomicIndicatorFactAttributeDTO> factAttributeDTOList;
}
