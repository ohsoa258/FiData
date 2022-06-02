package com.fisk.common.server.ocr.dto.aliyun.response;

/**
 * @author gy
 * @version 1.0
 * @description 单字信息
 * @date 2022/5/31 15:47
 */
public class CharInfo {
    /**
     * 单字文字
     */
    public String word;

    /**
     * 单字置信度
     */
    public Integer prob;

    /**
     * 单字左上角横坐标
     */
    public Integer x;

    /**
     * 单字左上角纵坐标
     */
    public Integer y;

    /**
     * 单字宽度
     */
    public Integer w;

    /**
     * 单字长度
     */
    public Integer h;
}
