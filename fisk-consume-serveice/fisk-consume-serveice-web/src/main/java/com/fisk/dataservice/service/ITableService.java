package com.fisk.dataservice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.dataservice.dto.tableservice.TableServicePageDataDTO;
import com.fisk.dataservice.dto.tableservice.TableServicePageQueryDTO;

/**
 * @author JianWenYang
 */
public interface ITableService {

    /**
     * 分页查询
     *
     * @param dto
     * @return
     */
    Page<TableServicePageDataDTO> getTableServiceListData(TableServicePageQueryDTO dto);

}
