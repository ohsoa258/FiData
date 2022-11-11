package com.fisk.license.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 许可证PO
 * @date 2022/11/9 14:03
 */
@Data
@TableName("tb_system_licence")
public class LicencePO extends BasePO {
    /**
     * 许可证
     */
    public String licence;
}
