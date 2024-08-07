package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datamodel.dto.versionsql.VersionSqlDTO;
import com.fisk.mdm.entity.TableVersionSqlPO;
import com.fisk.mdm.map.VersionSqlMap;
import com.fisk.mdm.mapper.TableVersionSqlPOMapper;
import com.fisk.mdm.service.ITableVersionSqlService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 56263
 * @description 针对表【tb_version_sql】的数据库操作Service实现
 * @createDate 2023-12-27 09:47:32
 */
@Service
public class TableVersionSqlServiceImpl extends ServiceImpl<TableVersionSqlPOMapper, TableVersionSqlPO>
        implements ITableVersionSqlService {

    /**
     * 通过表id和表类型获取表的所有版本sql
     *
     * @param tblId 表id
     * @return
     */
    @Override
    public List<VersionSqlDTO> getVersionSqlByTableIdAndType(Integer tblId) {
        LambdaQueryWrapper<TableVersionSqlPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TableVersionSqlPO::getTableId, tblId);
        List<TableVersionSqlPO> list = list(wrapper);
        return VersionSqlMap.INSTANCES.poListToDtoList(list);
    }

}




