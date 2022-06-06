package com.fisk.mdm.vo.entity;

import com.fisk.mdm.vo.attribute.AttributeMsgVO;
import lombok.Data;

import java.util.List;

/**
 * 实体ER图vo
 *
 * @author ChenYa
 * @date 2022/04/25
 */
@Data
public class EntityMsgVO {
    /**
     * id主键
     */
    private Integer id;

    /**
     * 归属模型id
     */
    private Integer modelId;

    /**
     * 名称
     */
    private String name;

    /**
     * 展示名称
     */
    private String displayName;

    /**
     * 字段信息
     */
    private List<AttributeMsgVO> attributeMsg;
}
