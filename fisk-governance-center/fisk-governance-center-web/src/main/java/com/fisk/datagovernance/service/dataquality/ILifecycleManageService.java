package com.fisk.datagovernance.service.dataquality;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.dataquality.lifecycle.LifecycleDTO;
import com.fisk.datagovernance.dto.dataquality.lifecycle.LifecycleEditDTO;
import com.fisk.datagovernance.dto.dataquality.lifecycle.LifecycleQueryDTO;
import com.fisk.datagovernance.entity.dataquality.LifecyclePO;
import com.fisk.datagovernance.vo.dataquality.lifecycle.LifecycleVO;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 生命周期接口
 * @date 2022/3/23 12:22
 */
public interface ILifecycleManageService extends IService<LifecyclePO> {
    /**
     * 分页查询
     *
     * @return 分页列表
     */
    List<LifecycleVO> getAllRule(LifecycleQueryDTO query);

    /**
     * 添加数据
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum addData(LifecycleDTO dto);

    /**
     * 编辑数据
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum editData(LifecycleEditDTO dto);

    /**
     * 删除数据
     *
     * @param id id
     * @return 执行结果
     */
    ResultEnum deleteData(int id);
}