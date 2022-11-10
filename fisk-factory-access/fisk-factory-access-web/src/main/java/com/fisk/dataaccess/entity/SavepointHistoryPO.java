package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author JianWenYang
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_savepoint_history")
public class SavepointHistoryPO extends BasePO {

    public Integer tableAccessId;

    /**
     * 检查点路径
     */
    public String savepointPath;

    /**
     * 检查点时间
     */
    public LocalDateTime savepointDate;

}
