package com.fisk.dataaccess.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.auth.dto.UserAuthDTO;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.api.ApiConfigDTO;
import com.fisk.dataaccess.dto.api.GenerateDocDTO;
import com.fisk.dataaccess.dto.api.ReceiveDataDTO;
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
     * 根据apiId生成api文档
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
    ResultEnum pushData(ReceiveDataDTO dto);

    /**
     * 获取实时api的临时token
     *
     * @param dto dto
     * @return 获取token结果
     */
    ResultEntity<String> getToken(UserAuthDTO dto);
}

