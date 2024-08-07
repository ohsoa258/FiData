package com.fisk.datamanagement.dto.DataSet;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datamanagement.vo.CodeCollectionVO;
import com.fisk.datamanagement.vo.CodeSetVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2024-01-30
 * @Description:
 */
@Data
public class CodeCollectionQueryDTO {
    /**
     * 搜索条件
     */
    @ApiModelProperty(value = "搜索条件")
    public String keyword;



    /**
     * 分页对象
     */
    @ApiModelProperty(value = "分页对象")
    public Page<CodeCollectionVO> page;
}
