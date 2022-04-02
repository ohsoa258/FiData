package com.fisk.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author WangYan
 * @date 2022/4/2 13:02
 */
@TableName("tb_dmp_images")
@Data
public class DmpImagesPO extends BasePO {

    private String imagePath;
}
