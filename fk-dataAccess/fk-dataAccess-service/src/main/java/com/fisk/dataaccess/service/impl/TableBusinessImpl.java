package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.dataaccess.entity.TableBusinessPO;
import com.fisk.dataaccess.mapper.TableBusinessMapper;
import com.fisk.dataaccess.service.ITableBusiness;
import org.springframework.stereotype.Service;

/**
 * @author Lock
 */
@Service
public class TableBusinessImpl extends ServiceImpl<TableBusinessMapper, TableBusinessPO> implements ITableBusiness {
}
