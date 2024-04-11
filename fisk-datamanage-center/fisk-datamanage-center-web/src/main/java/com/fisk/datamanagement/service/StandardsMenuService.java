package com.fisk.datamanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.standards.StandardsMenuDTO;
import com.fisk.datamanagement.dto.standards.StandardsTreeDTO;
import com.fisk.datamanagement.entity.StandardsMenuPO;

import java.util.List;


/**
 * 
 *
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2023-11-20 13:56:24
 */
public interface StandardsMenuService extends IService<StandardsMenuPO> {
    /**
     * 查看数据标准树形标签
     * @return
     */
    List<StandardsTreeDTO> getStandardsTree();

    /**
     * 添加或修改数据标准标签
     * @param standardsMenuDTO
     * @return
     */
    ResultEnum addorUpdateStandardsMenu(StandardsMenuDTO standardsMenuDTO);

    /**
     * 删除数据标准标签
     * @param ids
     * @return
     */
    ResultEnum delStandardsMenu(List<Integer> ids);

    /**
     * 查看数据标准树形标签--非懒加载
     * @return
     */
    List<StandardsTreeDTO> getStandardsAllTree();

    /**
     * 获取所有数据元标准menu-只要id和name
     * @return
     */
    List<StandardsMenuDTO> getStandardMenus();
}

