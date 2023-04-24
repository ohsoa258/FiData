package com.fisk.task.dto.doris;

import com.fisk.common.core.baseObject.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

/**
 * @author: DennyHui
 * CreateTime: 2021/8/2 12:32
 * Description:
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UpdateLogAndImportDataDTO extends BaseDTO {
    @ApiModelProperty(value = "id")
    public int id;
    @ApiModelProperty(value = "表名")
    public String tablename;
    @ApiModelProperty(value = "开始日期")
    public DateTime startdate;
    @ApiModelProperty(value = "结束日期")
    public DateTime enddate;
    @ApiModelProperty(value = "数据行")
    public int datarows;
    @ApiModelProperty(value = "状态")
    public int status;
    @ApiModelProperty(value = "唯一编码")
    public String code;
    @ApiModelProperty(value = "corn")
    public String corn;
    @ApiModelProperty(value = "错误信息")
    public String errordesc;
}
