package com.fisk.datamodel.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.dataaccess.dto.tablefield.TableFieldDTO;
import com.fisk.datamodel.dto.atomicindicator.AtomicIndicatorsQueryDTO;
import com.fisk.datamodel.dto.atomicindicator.AtomicIndicatorsResultDTO;
import com.fisk.datamodel.entity.IndicatorsPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper
public interface AtomicIndicatorsMapper extends FKBaseMapper<IndicatorsPO> {

    /**
     * 分页获取原子指标列表
     * @param page
     * @param query
     * @return 查询结果
     */
    Page<AtomicIndicatorsResultDTO> queryList(Page<AtomicIndicatorsResultDTO> page, @Param("query") AtomicIndicatorsQueryDTO query);


    /**
     * 根据关键字搜索字段列表
     * @param key
     * @return
     */
    List<TableFieldDTO> searchColumn(@Param("key") String key);
}
