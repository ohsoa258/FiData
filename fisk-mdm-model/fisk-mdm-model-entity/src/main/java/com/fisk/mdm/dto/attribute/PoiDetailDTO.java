package com.fisk.mdm.dto.attribute;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author: wangjian
 * @Date: 2023-08-24
 * @Description:
 */
@Data
public class PoiDetailDTO {
    private String id;
    private String title;
    private String address;
    private String tel;
    private String category;
    private String type;
    private Location location;
    private adInfo ad_info;

    @Data
    private class Location {
        private BigDecimal lat;
        private BigDecimal lng;
    }

    @Data
    private class adInfo {
        private Integer adcode;
        private String province;
        private String city;
        private String district;
    }

    private BigDecimal similarity;
}



