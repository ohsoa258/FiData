package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.dataaccess.entity.TableSyncmodePO;
import com.fisk.dataaccess.mapper.TableSyncmodeMapper;
import com.fisk.dataaccess.service.ITableSyncmode;
import org.springframework.stereotype.Service;

/**
 * @author Lock
 */
@Service
public class TableSyncmodeImpl extends ServiceImpl<TableSyncmodeMapper, TableSyncmodePO> implements ITableSyncmode {
}
