package com.fisk.mdm.vo.resultObject;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 结果对象
 *
 * @author ChenYa
 * @date 2022/04/27
 */
@Data
public class ResultObjectVO {

    /**
     * 属性组下所有属性
     */
    private List<ResultAttributeGroupVO> attributes;

    /**
     * 查询结果集
     */
    private List<Map<String, Object>> resultData;

    /**
     * 总条数
     */
    private Integer total;

    /**
     * 错误提示
     */
    private String errorMsg;

}
