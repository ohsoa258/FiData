package com.fisk.mdm.dto.entity;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author WangYan
 * @date 2022/4/15 11:09
 */
@Data
public class EntityPageDTO {

    @NotNull
    private Page<EntityDTO> page;
    private String name;
}
