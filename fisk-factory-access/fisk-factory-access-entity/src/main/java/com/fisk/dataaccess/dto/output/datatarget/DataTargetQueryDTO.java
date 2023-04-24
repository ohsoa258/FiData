package com.fisk.dataaccess.dto.output.datatarget;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.service.pageFilter.dto.FilterQueryDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 * @date 2022-08-17 16:17
 */
@Data
public class DataTargetQueryDTO {

    @ApiModelProperty(value = "数据目标应用id", required = true)
    public Long dataTargetAppId;
    /**
     * 数据过滤
     */
    @ApiModelProperty(value = "数据过滤")
    public List<FilterQueryDTO> queryDTOList;
    /**
     * 分页查询数据
     */
    @ApiModelProperty(value = "分页查询数据")
    public Page<DataTargetPageResultDTO> page;

}
