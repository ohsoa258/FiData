package com.fisk.datamanagement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.DataSet.CodeCollectionDTO;
import com.fisk.datamanagement.dto.DataSet.CodeCollectionQueryDTO;
import com.fisk.datamanagement.entity.CodeCollectionPO;
import com.fisk.datamanagement.vo.CodeCollectionVO;


/**
 * 
 *
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2024-08-01 14:37:05
 */
public interface CodeCollectionService extends IService<CodeCollectionPO> {

    ResultEnum addCodeCollection(CodeCollectionDTO dto);

    ResultEnum updateCodeCollection(CodeCollectionDTO dto);

    ResultEnum delCodeCollection(Integer id);

    Page<CodeCollectionVO> getCodeCollection(CodeCollectionQueryDTO query);

    Page<CodeCollectionVO> pageCollectionList(CodeCollectionQueryDTO query);
}

