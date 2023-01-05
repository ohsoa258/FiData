package com.fisk.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.license.dto.AuthorizeLicenceDTO;
import com.fisk.license.dto.VerifyLicenceDTO;
import com.fisk.license.entity.LicencePO;
import com.fisk.license.vo.LicenceVO;
import com.fisk.license.vo.VerifyLicenceVO;
import com.fisk.system.dto.license.LicenceDTO;
import com.fisk.system.entity.LicencePO;
import com.fisk.system.vo.license.QueryLicenceVO;

/**
 * @author dick
 * @version 1.0
 * @description ILicenseService
 * @date 2022/11/10 15:50
 */
public interface ILicenseService extends IService<LicencePO> {
    /**
     * 查询许可证
     */
    ResultEntity<QueryLicenceVO> getCompanyLicence(String keyWord);

    /**
     * 新增许可证
     */
    ResultEnum addCompanyLicence(LicenceDTO dto);

    /**
     * 编辑许可证
     */
    ResultEnum editCompanyLicence(LicenceDTO dto);

    /**
     * 删除许可证
     */
    ResultEnum deleteCompanyLicence(int id);
}
