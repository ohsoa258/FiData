package com.fisk.dataservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.dataservice.dto.tableapi.TableApiPageDataDTO;
import com.fisk.dataservice.dto.tableapi.TableApiPageQueryDTO;
import com.fisk.dataservice.dto.tableservice.TableServicePageDataDTO;
import com.fisk.dataservice.dto.tableservice.TableServicePageQueryDTO;
import com.fisk.dataservice.entity.TableApiServicePO;
import com.fisk.dataservice.vo.tableapi.ApiLogPageDTO;
import com.fisk.dataservice.vo.tableapi.ApiLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 
 *
 * @author wangjian
 * @date 2023-09-08 15:44:21
 */
@Mapper
public interface TableApiServiceMapper extends BaseMapper<TableApiServicePO> {
    /**
     * 分页查询表服务数据
     *
     * @param page
     * @param query
     * @return
     */
    Page<TableApiPageDataDTO> getTableApiListData(Page<TableApiPageDataDTO> page, @Param("query") TableApiPageQueryDTO query);
    Page<ApiLogVO> getApiLogs(Page<ApiLogVO> page, @Param("query") ApiLogPageDTO query);
}
