package com.fisk.dataaccess.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.dto.api.*;
import com.fisk.dataaccess.dto.api.httprequest.ApiHttpRequestDTO;
import com.fisk.dataaccess.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.dataaccess.entity.ApiConfigPO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-01-17 14:45:02
 */
public interface IApiConfig extends IService<ApiConfigPO> {

    /**
     * 回显: 根据id查询数据
     *
     * @param id id
     * @return 查询结果
     */
    ApiConfigDTO getData(long id);

    /**
     * 添加
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum addData(ApiConfigDTO dto);

    /**
     * 添加api下的物理表--保存or发布
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum addApiDetail(ApiConfigDTO dto);

    /**
     * 修改
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum editData(ApiConfigDTO dto);

    /**
     * 修改api下的物理表--保存or发布
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum editApiDetail(ApiConfigDTO dto);

    /**
     * 删除
     *
     * @param id id
     * @return 执行结果
     */
    ResultEnum deleteData(long id);

    /**
     * 根据appId获取api列表
     *
     * @param appId appId
     * @return list
     */
    List<ApiConfigDTO> getApiListData(long appId);

    /**
     * 生成文档(以api为单位)
     *
     * @param dto      dto
     * @param response response
     * @return 执行结果
     */
    ResultEnum generateDoc(GenerateDocDTO dto, HttpServletResponse response);

    /**
     * 推送数据
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEntity<Object> pushData(ReceiveDataDTO dto);

    /**
     * 获取实时api的临时token
     *
     * @param dto dto
     * @return 获取token结果
     */
    ResultEntity<String> getToken(ApiUserDTO dto);

    /**
     * 更新发布状态
     *
     * @param dto dto
     */
    void updateApiPublishStatus(ModelPublishStatusDTO dto);

    /**
     * 生成文档(以应用为单位)
     *
     * @param list     api集合
     * @param response response
     * @return 执行结果
     */
    ResultEnum generateAppPdfDoc(List<GenerateDocDTO> list, HttpServletResponse response);

    /**
     * 调度调用第三方api,接收数据,并导入到FiData平台
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum importData(ApiImportDataDTO dto);

    /**
     * api复制: 保存功能
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum copyApi(CopyApiDTO dto);

    /**
     * api复制: 获取下拉列表数据
     *
     * @return list
     */
    List<ApiSelectDTO> getAppAndApiList(int appType);

    /**
     * 获取http请求返回的结果
     *
     * @return String
     */
    String getHttpRequestResult(ApiHttpRequestDTO dto);

    /**
     * 根据apiId获取表字段信息
     *
     * @param apiId
     * @return
     */
    List<ApiColumnInfoDTO> getTableColumnInfoByApi(Integer apiId);

    List<ApiParameterDTO> getSourceFieldList(long tableAccessId);
    ResultEnum addSourceField(List<ApiParameterDTO> dto);

    ResultEnum deleteSourceField(long id);

    ResultEnum editSourceField(ApiParameterDTO dto);

    ResultEnum saveMapping(List<ApiFieldDTO> dto);


    /**
     * 根据apiId获取指定api
     * @param apiId
     * @return
     */
    ResultEntity<ApiConfigDTO> getOneApiById(Integer apiId);
}

