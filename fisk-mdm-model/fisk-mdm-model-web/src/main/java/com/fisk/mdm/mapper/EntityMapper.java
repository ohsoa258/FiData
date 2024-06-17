package com.fisk.mdm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.mdm.dto.dataops.TableQueryDTO;
import com.fisk.mdm.entity.EntityPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author WangYan
 * @date 2022/4/2 17:48
 */
@Mapper
public interface EntityMapper extends BaseMapper<EntityPO> {
    /**
     * 数据运维根据表名获取表信息
     * @param tableName
     * @return
     */
    TableQueryDTO getTableInfo(@Param("tableName") String tableName);

    Integer getEntityTotal();
}
