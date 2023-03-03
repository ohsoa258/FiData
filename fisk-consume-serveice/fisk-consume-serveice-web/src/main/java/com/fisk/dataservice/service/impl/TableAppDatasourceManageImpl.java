package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.dataservice.entity.TableAppDatasourcePO;
import com.fisk.dataservice.mapper.TableAppDatasourceMapper;
import com.fisk.dataservice.service.ITableAppDatasourceManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TableAppDatasourceManageImpl
        extends ServiceImpl<TableAppDatasourceMapper, TableAppDatasourcePO>
        implements ITableAppDatasourceManageService {
}
