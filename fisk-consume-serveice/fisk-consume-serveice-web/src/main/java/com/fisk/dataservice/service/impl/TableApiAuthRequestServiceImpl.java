package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.dataservice.entity.TableApiAuthRequestPO;
import com.fisk.dataservice.mapper.TableApiAuthRequestMapper;
import com.fisk.dataservice.service.ITableApiAuthRequestService;
import org.springframework.stereotype.Service;

@Service("tableApiAuthRequestService")
public class TableApiAuthRequestServiceImpl extends ServiceImpl<TableApiAuthRequestMapper, TableApiAuthRequestPO> implements ITableApiAuthRequestService {


}
