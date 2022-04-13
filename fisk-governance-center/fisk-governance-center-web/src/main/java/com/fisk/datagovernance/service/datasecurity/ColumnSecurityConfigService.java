package com.fisk.datagovernance.service.datasecurity;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.datasecurity.columnsecurityconfig.ColumnSecurityConfigDTO;
import com.fisk.datagovernance.dto.datasecurity.columnsecurityconfig.ColumnSecurityConfigUserAssignmentDTO;
import com.fisk.datagovernance.dto.datasecurity.columnsecurityconfig.ColumnSecurityConfigValidDTO;
import com.fisk.datagovernance.entity.datasecurity.ColumnSecurityConfigPO;

import java.util.List;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:33
 */
public interface ColumnSecurityConfigService extends IService<ColumnSecurityConfigPO> {

    /**
     * 获取列级配置列表
     * @param tableId
     * @return
     */
    List<ColumnSecurityConfigUserAssignmentDTO> listColumnSecurityConfig(String tableId);

    /**
     * 添加列级配置
     * @param dto
     * @return
     */
    ResultEnum saveColumnSecurityConfig(ColumnSecurityConfigUserAssignmentDTO dto);

    /**
     * 回显: 根据id查询数据
     *
     * @param id id
     * @return 查询结果
     */
    ColumnSecurityConfigDTO getData(long id);

    /**
     * 修改
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum editData(ColumnSecurityConfigUserAssignmentDTO dto);

    /**
     * 删除
     *
     * @param id id
     * @return 执行结果
     */
    ResultEnum deleteData(long id);

    /**
     * 修改有效状态
     * @param dto
     * @return
     */
    ResultEnum updateValid(ColumnSecurityConfigValidDTO dto);

}

