package com.fisk.datagovernance.service.dataquality;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.datagovernance.entity.dataquality.AttachmentInfoPO;

import javax.servlet.http.HttpServletResponse;

/**
 * @author dick
 * @version 1.0
 * @description 附件接口
 * @date 2022/8/15 9:55
 */
public interface IAttachmentInfoManageService extends IService<AttachmentInfoPO> {

    /**
     * 下载Excel文件
     *
     * @return 执行结果
     */
    void downloadExcelFile(String objectId,String category, HttpServletResponse response);

}
