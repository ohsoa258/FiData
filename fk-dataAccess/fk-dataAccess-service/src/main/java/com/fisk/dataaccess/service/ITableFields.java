package com.fisk.dataaccess.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.dataaccess.dto.datareview.DataReviewQueryDTO;
import com.fisk.dataaccess.entity.TableFieldsPO;
import com.fisk.dataaccess.vo.datareview.DataReviewVO;

/**
 * @author Lock
 */
public interface ITableFields extends IService<TableFieldsPO> {
    /**
     * 分页
     * @param query query
     * @return 分页结果
     */
    Page<DataReviewVO> listData(DataReviewQueryDTO query);
}
