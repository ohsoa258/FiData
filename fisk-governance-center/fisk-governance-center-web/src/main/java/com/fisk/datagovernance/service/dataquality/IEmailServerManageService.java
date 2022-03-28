package com.fisk.datagovernance.service.dataquality;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.response.ResultEnum;
import com.fisk.datagovernance.dto.dataquality.emailserver.EmailServerDTO;
import com.fisk.datagovernance.dto.dataquality.emailserver.EmailServerEditDTO;
import com.fisk.datagovernance.dto.dataquality.emailserver.EmailServerQueryDTO;
import com.fisk.datagovernance.entity.dataquality.EmailServerPO;
import com.fisk.datagovernance.vo.dataquality.emailserver.EmailServerVO;

/**
 * @author dick
 * @version 1.0
 * @description 邮件服务器配置接口
 * @date 2022/3/23 12:22
 */
public interface IEmailServerManageService extends IService<EmailServerPO> {
    /**
     * 分页查询
     *
     * @return 分页列表
     */
    Page<EmailServerVO> getAll(EmailServerQueryDTO query);

    /**
     * 添加数据
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum addData(EmailServerDTO dto);

    /**
     * 编辑数据
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum editData(EmailServerEditDTO dto);

    /**
     * 删除数据
     *
     * @param id id
     * @return 执行结果
     */
    ResultEnum deleteData(int id);
}