package com.fisk.chartvisual.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import com.fisk.common.entity.BaseSqlServerPO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报表文件夹
 * @author gy
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("tb_folder")
public class FolderPO extends BaseSqlServerPO {
    public String name;
    public Long pid;
}
