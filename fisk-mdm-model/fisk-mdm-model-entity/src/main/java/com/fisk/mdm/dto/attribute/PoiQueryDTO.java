package com.fisk.mdm.dto.attribute;

import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-08-24
 * @Description:
 */
@Data
public class PoiQueryDTO {
    private String searchArea;
    private String categoryType;
    private String keyword;
    @Data
    private class SearchArea{
        private Integer id;
        private String fullname;
    }
}
