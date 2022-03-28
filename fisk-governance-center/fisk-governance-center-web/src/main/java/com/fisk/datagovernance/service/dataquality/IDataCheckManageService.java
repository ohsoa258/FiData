package com.fisk.datagovernance.service.dataquality;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.response.ResultEnum;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckEditDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckQueryDTO;
import com.fisk.datagovernance.entity.dataquality.DataCheckPO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckVO;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验接口
 * @date 2022/3/23 12:22
 */
public interface IDataCheckManageService extends IService<DataCheckPO> {
    /**
     * 分页查询
     *
     * @return 分页列表
     */
    Page<DataCheckVO> getAll(DataCheckQueryDTO query);

    /**
     * 添加数据
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum addData(DataCheckDTO dto);

    /**
     * 编辑数据
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum editData(DataCheckEditDTO dto);

    /**
     * 删除数据
     *
     * @param id id
     * @return 执行结果
     */
    ResultEnum deleteData(int id);
}