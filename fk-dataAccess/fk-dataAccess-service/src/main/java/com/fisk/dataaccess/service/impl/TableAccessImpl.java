package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.dataaccess.entity.TableAccessPO;
import com.fisk.dataaccess.mapper.TableAccessMapper;
import com.fisk.dataaccess.service.ITableAccess;
import org.springframework.stereotype.Service;

/**
 * @author: Lock
 */
@Service
public class TableAccessImpl extends ServiceImpl<TableAccessMapper, TableAccessPO> implements ITableAccess {
}
