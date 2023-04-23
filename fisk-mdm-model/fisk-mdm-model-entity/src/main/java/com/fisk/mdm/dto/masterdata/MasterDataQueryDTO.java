package com.fisk.mdm.dto.masterdata;

import com.fisk.common.service.pageFilter.dto.FilterQueryDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class MasterDataQueryDTO extends MasterDataBaseDTO {

    /**
     * 当前页数
     */
    @ApiModelProperty(value = "当前页数")
    private Integer pageIndex;

    /**
     * 每页条数
     */
    @ApiModelProperty(value = "每页条数")
    private Integer pageSize;

    /**
     * 是否导出
     */
    @ApiModelProperty(value = "是否导出")
    private Boolean export;

    /**
     * 属性组
     */
    @ApiModelProperty(value = "属性组")
    private List<Integer> attributeGroups;

    /**
     * 自定义视图id
     */
    @ApiModelProperty(value = "自定义视图id")
    private Integer viewId;

    @ApiModelProperty(value = "有效性")
    private Integer validity;
    /**
     * 自定义筛选条件
     */
    @ApiModelProperty(value = "自定义筛选条件")
    private List<FilterQueryDTO> filterQuery;



}
