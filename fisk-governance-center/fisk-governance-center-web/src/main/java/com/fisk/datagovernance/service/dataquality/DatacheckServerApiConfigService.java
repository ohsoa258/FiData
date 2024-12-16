package com.fisk.datagovernance.service.dataquality;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.dataquality.datacheck.*;
import com.fisk.datagovernance.entity.dataquality.DatacheckServerApiConfigPO;
import com.fisk.datagovernance.vo.dataquality.datacheck.ApiSeverSubVO;
import com.fisk.datagovernance.vo.dataquality.datacheck.AppServiceCountVO;
import com.fisk.datagovernance.dto.dataquality.datacheck.api.RequstDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.api.TokenDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * 
 *
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2024-10-23 15:33:27
 */
public interface DatacheckServerApiConfigService extends IService<DatacheckServerApiConfigPO> {

    /**
     * 获取token
     * @param dto dto
     * @return token
     */
    ResultEntity<Object> getToken(TokenDTO dto);

    /**
     * 获取数据
     * @param dto 请求参数
     * @return 数据
     */
    ResultEntity<Object> getData(RequstDTO dto);

    PageDTO<ApiSeverSubVO> getApiSubAll(ApiSubQueryDTO dto);


    ResultEnum editApiField(ApiFieldEditDTO dto);

    ResultEnum editApiState(ApiStateDTO dto);

    /**
     * 重置应用密码
     * @param dto 应用密码信息
     * @return 执行结果
     */
    ResultEnum resetPwd(AppPwdResetDTO dto);

    /**
     * 生成文档
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum createDoc(CreateAppApiDocDTO dto, HttpServletResponse response);

    List<AppServiceCountVO> getApiAppServiceCount();

    ResultEnum delApi(Integer apiId);
}

