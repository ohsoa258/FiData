package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.dataservice.dto.tableservice.TableServicePageDataDTO;
import com.fisk.dataservice.dto.tableservice.TableServicePageQueryDTO;
import com.fisk.dataservice.mapper.TableServiceMapper;
import com.fisk.dataservice.service.ITableService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Service
public class TableServiceImpl implements ITableService {

    @Resource
    TableServiceMapper mapper;

    @Override
    public Page<TableServicePageDataDTO> getTableServiceListData(TableServicePageQueryDTO dto) {
        return mapper.getTableServiceListData(dto.page, dto);
    }
}
