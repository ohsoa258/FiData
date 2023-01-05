package com.fisk.license.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.license.dto.AuthorizeLicenceDTO;
import com.fisk.license.dto.VerifyLicenceDTO;
import com.fisk.license.entity.LicencePO;
import com.fisk.license.vo.LicenceVO;
import com.fisk.license.vo.VerifyLicenceVO;

/**
 * @author dick
 * @version 1.0
 * @description ILicenseService
 * @date 2022/11/10 15:50
 */
public interface ILicenseService extends IService<LicencePO> {
    /**
     * 校验Url是否有访问权限
     */
    VerifyLicenceVO verifyCompanyLicenceByUrl(VerifyLicenceDTO dto);

    /**
     * 设置许可证
     */
    ResultEntity<String> setCompanyLicence(AuthorizeLicenceDTO dto);

    /**
     * 查询许可证
     */
    ResultEntity<LicenceVO> getCompanyLicence();

    /**
     * 获取计算机Mac地址
     */
    ResultEntity<String> getMacAddress();
}
