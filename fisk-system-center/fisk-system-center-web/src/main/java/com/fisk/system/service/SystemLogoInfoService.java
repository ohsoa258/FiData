package com.fisk.system.service;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.system.dto.SystemLogoInfoDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * @ClassName:
 * @Author: SonJianJian
 * @Date: 2023
 * @Copyright: 2023 by SongJianJian
 * @Description:
 **/
public interface SystemLogoInfoService {

    /**
     * 存储系统logo及系统名称
     *
     * @param systemLogoInfoDTO
     * @param file
     * @return
     */
    ResultEnum saveLogoInfo(SystemLogoInfoDTO systemLogoInfoDTO, MultipartFile file);

    /**
     * 获取系统logo及系统名称
     * @return
     */
    ResultEntity<Object> getLogoInfo();

    /**
     * 更新系统logo及系统名称
     *
     * @param systemLogoInfoDTO
     * @param file
     * @return
     */
    ResultEnum updateLogoInfo(SystemLogoInfoDTO systemLogoInfoDTO, MultipartFile file);
}
