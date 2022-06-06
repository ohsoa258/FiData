package com.fisk.common.server.ocr.dto.aliyun.response;

import lombok.Data;

import java.util.List;

/**
 * @author gy
 * @version 1.0
 * @description 文本块详情
 * @date 2022/5/31 15:44
 */
@Data
public class WorksInfo {
    /**
     * 文字块
     */
    public String word;

    /**
     * 文字块的位置，按照文字块四个角的坐标顺时针排列，分别为左上XY坐标、右上XY坐标、右下XY坐标、左下XY坐标
     */
    public List<Position> pos;

    public Integer direction;

    public Integer angle;

    public Integer x;

    public Integer y;

    public Integer width;

    public Integer height;

    /**
     * 置信度
     */
    public Integer prob;

    /**
     * 单字信息
     */
    public List<CharInfo> charInfo;
}
