package com.fisk.datagovernance.service.datasecurity;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.datasecurity.RowSecurityConfigDTO;
import com.fisk.datagovernance.entity.datasecurity.RowSecurityConfigPO;

import java.util.List;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:33
 */
public interface RowSecurityConfigService extends IService<RowSecurityConfigPO> {

    /**
     * 回显: 根据id查询数据
     *
     * @param id id
     * @return 查询结果
     */
    RowSecurityConfigDTO getData(long id);

    /**
     * 添加
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum addData(RowSecurityConfigDTO dto);

    /**
     * 修改
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum editData(RowSecurityConfigDTO dto);

    /**
     * 删除
     *
     * @param id id
     * @return 执行结果
     */
    ResultEnum deleteData(long id);

    /**
     * 修改表级缺省配置
     *
     * @param defaultConfig defaultConfig
     * @return 执行结果
     */
    ResultEnum editDefaultConfig(long defaultConfig);

    /**
     * 获取行级安全列表
     *
     * @return list
     */
    List<RowSecurityConfigDTO> getList();
}

