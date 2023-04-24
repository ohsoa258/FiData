package com.fisk.dataaccess.dto.datatargetapp;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.service.pageFilter.dto.FilterQueryDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DataTargetAppQueryDTO {

    /**
     * 数据过滤
     */
    @ApiModelProperty(value = "数据过滤")
    public List<FilterQueryDTO> queryDTOList;

    /**
     * 分页查询数据
     */
    @ApiModelProperty(value = "分页查询数据")
    public Page<DataTargetAppDTO> page;

}
