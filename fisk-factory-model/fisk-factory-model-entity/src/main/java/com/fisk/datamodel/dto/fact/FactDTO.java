package com.fisk.datamodel.dto.fact;

import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class FactDTO {
    @ApiModelProperty(value = "id")
    public int id;
    /**
     * 业务域id
     */
    @ApiModelProperty(value = "业务域id")
    public int businessId;
    /**
     * 业务过程id
     */
    @ApiModelProperty(value = "业务过程id")
    public int businessProcessId;
    /**
     * 事实表名称
     */
    @ApiModelProperty(value = "事实表名称")
    public String factTabName;
    /**
     * 事实表中文名称
     */
    @ApiModelProperty(value = "事实表中文名称")
    public String factTableCnName;
    /**
     * 事实表描述
     */
    @ApiModelProperty(value = "事实表描述")
    public String factTableDesc;

    /*
     * 接入的增量时间参数
     */
    @ApiModelProperty(value = "接入的增量时间参数")
    public List<DeltaTimeDTO> deltaTimes;

}
