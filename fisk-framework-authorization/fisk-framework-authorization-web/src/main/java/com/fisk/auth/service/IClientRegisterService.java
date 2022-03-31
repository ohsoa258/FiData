package com.fisk.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.auth.dto.clientregister.ClientRegisterDTO;
import com.fisk.auth.dto.clientregister.ClientRegisterQueryDTO;
import com.fisk.auth.entity.ClientRegisterPO;
import com.fisk.auth.vo.ClientRegisterVO;
import com.fisk.common.filter.dto.FilterFieldDTO;
import com.fisk.common.core.response.ResultEnum;

import java.util.List;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-04 16:46:24
 */
public interface IClientRegisterService extends IService<ClientRegisterPO> {

    /**
     * 回显: 根据id查询数据
     *
     * @param id id
     * @return 查询结果
     */
    ClientRegisterDTO getData(long id);

    /**
     * 添加
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum addData(ClientRegisterDTO dto);

    /**
     * 修改
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum editData(ClientRegisterDTO dto);

    /**
     * 删除
     *
     * @param id id
     * @return 执行结果
     */
    ResultEnum deleteData(long id);

    /**
     * 获取所有已注册的客户端信息
     *
     * @return list
     */
    List<String> getClientInfoList();

    /**
     * 筛选器
     *
     * @param query 查询条件
     * @return 筛选器分页集合
     */
    Page<ClientRegisterVO> listData(ClientRegisterQueryDTO query);

    /**
     * 获取筛选器表字段
     *
     * @return 字段
     */
    List<FilterFieldDTO> getColumn();
}

