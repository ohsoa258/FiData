package com.fisk.datamodel.dto.widetableconfig;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class WideTableListDTO {

    public long id;

    public String name;

    public List<String> fieldList;

}
