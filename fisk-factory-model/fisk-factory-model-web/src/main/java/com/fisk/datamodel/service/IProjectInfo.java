package com.fisk.datamodel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.ProjectInfoDTO;
import com.fisk.datamodel.entity.ProjectInfoPO;

import java.util.Map;

/**
 * @author Lock
 */
public interface IProjectInfo extends IService<ProjectInfoPO> {

    /**
     * 添加项目空间
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum addData(ProjectInfoDTO dto);

    /**
     * 回显数据: 根据id查询
     *
     * @param id id
     * @return 查询结果
     */
    ProjectInfoDTO getDataById(long id);


    /**
     * 业务域修改
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum updateProjectInfo(ProjectInfoDTO dto);

    /**
     * 删除业务域
     *
     * @param id id
     * @return 执行结果
     */
    ResultEnum deleteDataById(long id);


    /**
     * 分页查询
     *
     * @param key  搜索条件
     * @param page 当前页码
     * @param rows 每页显示条数
     * @return 查询结果
     */
    Page<Map<String, Object>> listData(String key, Integer page, Integer rows);
}
