package com.fisk.dataaccess.dto.datareview;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.dataaccess.vo.datareview.DataReviewVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class DataReviewPageDTO {

    @ApiModelProperty(value = "where")
    public String where;

    @ApiModelProperty(value = "分页")
    public Page<DataReviewVO> page;

}
