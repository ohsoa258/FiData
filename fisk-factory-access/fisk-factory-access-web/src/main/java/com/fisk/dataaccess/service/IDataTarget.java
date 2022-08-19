package com.fisk.dataaccess.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.dataaccess.dto.output.datatarget.DataTargetAddDTO;
import com.fisk.dataaccess.dto.output.datatarget.DataTargetPageResultDTO;
import com.fisk.dataaccess.dto.output.datatarget.DataTargetQueryDTO;
import com.fisk.dataaccess.vo.output.datatarget.DataTargetVO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IDataTarget {

    /**
     * 分页查询数据目标数据
     *
     * @param dto
     * @return
     */
    Page<DataTargetPageResultDTO> getDataList(DataTargetQueryDTO dto);

    /**
     * 获取数据目标字段
     *
     * @return
     */
    List<FilterFieldDTO> getDataTargetColumn();

    /**
     * 新增数据目标
     *
     * @param dto
     * @return
     */
    ResultEnum addDataTarget(DataTargetAddDTO dto);

    /**
     * 删除数据目标
     *
     * @param id
     * @return
     */
    ResultEnum delete(Long id);

    /**
     * 数据目标详情
     *
     * @param id
     * @return
     */
    DataTargetVO getDataTarget(Long id);

    /**
     * 编辑数据目标
     *
     * @param vo
     * @return
     */
    ResultEnum updateDataTarget(DataTargetVO vo);


}
