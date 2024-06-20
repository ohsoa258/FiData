package com.fisk.datamanagement.dto.category;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.service.pageFilter.dto.FilterQueryDTO;
import com.fisk.datamanagement.dto.classification.BusinessTargetinfoMenuDTO;
import com.fisk.dataservice.vo.tableservice.TableAppVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2024-06-17
 * @Description:
 */
@Data
public class CategoryQueryDTO {
        /**
         * 筛选器对象
         */
        @ApiModelProperty(value = "筛选器对象")
        public List<FilterQueryDTO> dto;

//        /**
//         * 分页对象
//         */
//        @ApiModelProperty(value = "分页对象")
//        public Page<BusinessTargetinfoMenuDTO> page;
}
