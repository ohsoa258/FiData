package com.fisk.dataaccess.dto.datareview;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.service.pageFilter.dto.FilterQueryDTO;
import com.fisk.dataaccess.vo.datareview.DataReviewVO;
import lombok.Data;

import java.util.List;

/**
 * @author Lock
 */
@Data
public class DataReviewQueryDTO {

    /**
     * 查询字段值
     */
    public String key;
    public List<FilterQueryDTO> dto;
    /**
     * 分页,返回给前端的数据对象
     */
    public Page<DataReviewVO> page;

}
