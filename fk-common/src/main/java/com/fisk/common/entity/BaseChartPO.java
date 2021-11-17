package com.fisk.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/11/17 10:19
 */
@Data
public class BaseChartPO {

    @TableId(value = "id", type = IdType.AUTO)
    public long id;

    @TableLogic
    private int delFlag;
}
