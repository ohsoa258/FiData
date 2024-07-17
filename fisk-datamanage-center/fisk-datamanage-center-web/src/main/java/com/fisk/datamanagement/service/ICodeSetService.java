package com.fisk.datamanagement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.DataSet.CodeSetDTO;
import com.fisk.datamanagement.dto.DataSet.CodeSetQueryDTO;
import com.fisk.datamanagement.entity.CodeSetPO;
import com.fisk.datamanagement.vo.CodeSetVO;


/**
 * 
 *
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2024-01-30 10:09:37
 */
public interface ICodeSetService extends IService<CodeSetPO> {

    Page<CodeSetVO> getAll(CodeSetQueryDTO query);

    ResultEnum addCodeSet(CodeSetDTO dto);

    Integer getGenerateCode();

    ResultEnum updateCodeSet(CodeSetDTO dto);

    ResultEnum delCodeSet(Integer id);
}

