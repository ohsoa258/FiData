package com.fisk.dataaccess.dto.datareview;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.dataaccess.vo.datareview.DataReviewVO;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class DataReviewPageDTO {

    public String where;
    public Page<DataReviewVO> page;

}
