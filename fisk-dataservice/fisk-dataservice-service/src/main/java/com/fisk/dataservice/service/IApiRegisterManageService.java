package com.fisk.dataservice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.filter.dto.FilterFieldDTO;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.dto.api.*;
import com.fisk.dataservice.entity.ApiConfigPO;
import com.fisk.dataservice.vo.api.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * api注册接口
 * @author dick
 */
public interface IApiRegisterManageService {

    /**
     * 分页查询
     * @param page 分页条件
     * @return 应用列表
     */
    Page<ApiRegisterVO> getAll(Page<ApiRegisterVO> page);

    /**
     * 添加数据
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum addData(ApiRegisterDTO dto);

    /**
     * 编辑数据
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum editData(ApiRegisterEditDTO dto);

    /**
     * 删除数据
     * @param apiId apiId
     * @return 执行结果
     */
    ResultEnum deleteData(Integer apiId);

    /**
     * 查询api信息
     * @param apiId apiId
     * @return api详情
     */
    ApiRegisterDetailVO detail(Integer apiId);

    /**
     * 查询api字段列表
     * @param apiId apiId
     * @return api字段列表
     */
    List<FieldConfigVO> getFieldAll(Integer apiId);

    /**
     * 设置字段属性
     * @param dto 字段信息
     * @return 执行结果
     */
    ResultEnum  setField( List<FieldConfigEditDTO> dto);

    /**
     * 预览
     * @param appId apiId
     * @return 预览结果
     */
    ResultEntity<Object> preview(Integer appId);
}
