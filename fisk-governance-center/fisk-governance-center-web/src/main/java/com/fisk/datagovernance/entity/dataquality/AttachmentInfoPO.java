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
@TableName("tb_attachment_info")
public class AttachmentInfoPO extends BasePO {
    /**
     * 原名称
     */
    public String originalName;

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
     * 附件类别
     * 100: 质量校验summary报告
     * 200: 数据清洗报告
     * 300: 智能发现报告
     * 400: 数据检查日志报告
     * 500: 数据运维生成导入模板
     * 600: 质量校验规则校验明细报告
     */
    public int category;

    /**
     * 业务ID
     */
    public String objectId;
}
