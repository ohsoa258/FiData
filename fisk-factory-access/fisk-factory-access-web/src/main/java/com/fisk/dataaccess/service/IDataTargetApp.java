package com.fisk.dataaccess.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.dataaccess.dto.datatargetapp.DataTargetAppDTO;
import com.fisk.dataaccess.dto.datatargetapp.DataTargetAppQueryDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IDataTargetApp {

    /**
     * 分页查询目标数据应用
     *
     * @param dto
     * @return
     */
    Page<DataTargetAppDTO> getDataTargetAppList(DataTargetAppQueryDTO dto);

    /**
     * 新增目标数据应用
     *
     * @param dto
     * @return
     */
    ResultEnum addDataTargetApp(DataTargetAppDTO dto);

    /**
     * 获取目标数据应用详情
     *
     * @param id
     * @return
     */
    DataTargetAppDTO getDataTargetApp(long id);

    /**
     * 编辑目标数据应用
     *
     * @param dto
     * @return
     */
    ResultEnum updateDataTargetApp(DataTargetAppDTO dto);

    /**
     * 删除目标数据应用
     *
     * @param id
     * @return
     */
    ResultEnum deleteDataTargetApp(long id);

    /**
     * 获取数据目标字段
     *
     * @return
     */
    List<FilterFieldDTO> getDataTargetAppColumn();

}