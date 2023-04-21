package com.fisk.datamodel.dto.businessprocess;

import com.fisk.datamodel.dto.atomicindicator.IndicatorsDataDTO;
import com.fisk.datamodel.dto.fact.FactDataDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class BusinessProcessListDTO {

    @ApiModelProperty(value = "id")
    public long id;
    /**
     * 业务过程中文名称
     */
    @ApiModelProperty(value = "业务过程中文名称")
    public String businessProcessCnName;
    /**
     * 发布状态：1:未发布、2：发布成功、3：发布失败
     */
    //public int isPublish;
    /**
     * 业务过程下事实列表
     */
    @ApiModelProperty(value = "业务过程下事实列表")
    public List<FactDataDTO> factList;

}
