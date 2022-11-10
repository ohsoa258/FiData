package com.fisk.mdm.vo.complextype;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author JianWenYang
 */
@Data
public class GeographyVO {

    /**
     * 唯一编码
     */
    private String code;

    /**
     * 经度
     */
    private String lng;

    /**
     * 维度
     */
    private String lat;

    /**
     * 地图类型：0:高德类型，1:百度类型
     */
    private Integer map_type;

    private LocalDateTime create_time;

    private Long create_user;

    /**
     * 版本id
     */
    private Integer fidata_version_id;

}
