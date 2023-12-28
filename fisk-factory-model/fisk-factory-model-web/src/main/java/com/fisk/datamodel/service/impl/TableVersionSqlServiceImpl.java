package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datamodel.dto.versionsql.VersionSqlDTO;
import com.fisk.datamodel.entity.TableVersionSqlPO;
import com.fisk.datamodel.map.versionsql.VersionSqlMap;
import com.fisk.datamodel.mapper.TableVersionSqlPOMapper;
import com.fisk.datamodel.service.ITableVersionSqlService;
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
     * @param tblType  0维度 1事实
     * @return
     */
    @Override
    public List<VersionSqlDTO> getVersionSqlByTableIdAndType(Integer tblId, Integer tblType) {
        LambdaQueryWrapper<TableVersionSqlPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TableVersionSqlPO::getTableId, tblId)
                .eq(TableVersionSqlPO::getTableType, tblType);
        List<TableVersionSqlPO> list = list(wrapper);
        return VersionSqlMap.INSTANCES.poListToDtoList(list);
    }

}




