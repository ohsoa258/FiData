package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version v1.0
 * @description 附件信息
 * @date 2022/3/22 14:51
 */
@Data
@TableName("tb_attachmentInfo")
public class AttachmentInfoPO  extends BasePO {
    /**
     * 原名称
     */
    public  String originalName;

    /**
     * 当前名称
     */
    public String currentFileName;

    /**
     * 附件后缀
     */
    public String extensionName;

    /**
     * 相对路径
     */
    public String relativePath;

    /**
     * 绝对路径
     */
    public String absolutePath;

    /**
     * 附件类别 、
     * 100：数据校验质量报告
     * 200 业务清洗质量报告
     * 300 生命周期质量报告
     */
    public int category;

    /**
     * 业务ID
     */
    public String objectId;
}
