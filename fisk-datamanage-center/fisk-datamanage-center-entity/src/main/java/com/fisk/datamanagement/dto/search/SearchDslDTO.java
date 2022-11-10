package com.fisk.datamanagement.dto.search;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class SearchDslDTO {

    public String query;

    public Integer limit;

    public Integer offset;

}
