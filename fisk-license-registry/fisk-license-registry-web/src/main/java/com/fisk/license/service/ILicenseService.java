package com.fisk.license.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.license.dto.LicenceDTO;
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
     * 生成许可证
     */
    ResultEntity<LicenceVO> createCompanyLicence(LicenceDTO dto);
}
