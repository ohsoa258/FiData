package com.fisk.mdm.dto.masterdata;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 * date 2022/05/07 11:06
 */
@Data
public class ImportDataQueryDTO {

    @ApiModelProperty(value = "尺寸大小")
    private Integer pageSize;

    @ApiModelProperty(value = "页码索引")
    private Integer pageIndex;

    @ApiModelProperty(value = "主题Id")
    private Integer entityId;

    @ApiModelProperty(value = "键")
    private String key;

    /**
     * 上传状态
     */
    @ApiModelProperty(value = "上传状态")
    private List<Integer> status;

    /**
     * 上传逻辑
     */
    @ApiModelProperty(value = "上传逻辑")
    private List<Integer> syncType;

}
