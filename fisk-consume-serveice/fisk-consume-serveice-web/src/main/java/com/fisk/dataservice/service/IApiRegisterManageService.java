package com.fisk.dataservice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.dto.api.*;
import com.fisk.dataservice.dto.appserviceconfig.AppTableServiceConfigDTO;
import com.fisk.dataservice.entity.ApiConfigPO;
import com.fisk.dataservice.vo.api.*;
import com.fisk.dataservice.vo.fileservice.FileServiceVO;
import com.fisk.dataservice.vo.tableservice.TableServiceVO;

import java.util.List;

/**
 * api注册接口
 *
 * @author dick
 */
public interface IApiRegisterManageService extends IService<ApiConfigPO> {

    /**
     * 分页查询
     *
     * @return 应用列表
     */
    Page<ApiConfigVO> getAll(ApiRegisterQueryDTO query);

    /**
     * 分页查询所有api订阅
     *
     * @return 应用列表
     */
    PageDTO<ApiSubVO> getApiSubAll(ApiSubQueryDTO query);

    /**
     * 添加数据
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum addData(ApiRegisterDTO dto);

    /**
     * 编辑数据
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum editData(ApiRegisterEditDTO dto);

    /**
     * 删除数据
     *
     * @param apiId apiId
     * @return 执行结果
     */
    ResultEnum deleteData(int apiId);

    /**
     * 查询api信息
     *
     * @param apiId apiId
     * @return api详情
     */
    ResultEntity <ApiRegisterDetailVO> detail(int apiId);

    /**
     * 查询api字段列表
     *
     * @param apiId apiId
     * @return api字段列表
     */
    List<FieldConfigVO> getFieldAll(int apiId);

    /**
     * 设置字段属性
     *
     * @param dto 字段信息
     * @return 执行结果
     */
    ResultEnum setField(List<FieldConfigEditDTO> dto);

    /**
     * 预览
     *
     * @param dto 请求参数
     * @return 预览结果
     */
    ApiPreviewVO preview(ApiPreviewDTO dto);

    /**
     * 分页获取订阅表服务
     *
     * @param dto
     * @return
     */
    PageDTO<TableServiceVO> getTableServiceSubAll(ApiSubQueryDTO dto);

    /**
     * 分页获取订阅文件服务
     *
     * @param dto
     * @return
     */
    PageDTO<FileServiceVO> getFileServiceSubAll(ApiSubQueryDTO dto);

    /**
     * 表服务引入配置
     *
     * @param dtoList
     * @return
     */
    ResultEnum appTableServiceConfig(List<AppTableServiceConfigDTO> dtoList);

}
