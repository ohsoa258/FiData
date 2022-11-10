package com.fisk.common.service.mdmBEOperate;

import java.util.List;
import java.util.Map;

/**
 * @author JianWenYang
 */
public interface IBuildCodeCommand {

    /**
     * 创建编码
     *
     * @return
     */
    String createCode();

    /**
     * 获取当前时间戳
     * @return
     */
    String getCurrentTime();

    /**
     * 固定值(需要组合独立编码一起使用)
     * @param value
     * @return
     */
    String fixedValue(String value);

    /**
     * 复制当前行某个字段的值(需要组合独立编码一起使用)
     * @param field  字段
     * @param result 当前行数据的结果集
     * @return
     */
    String getFiledValue(String field, List<Map<String,String>> result);
}
