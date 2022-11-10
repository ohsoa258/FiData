package com.fisk.datamanagement.dto.assetsdirectory;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 * @date 2022-06-20 17:56
 */
@Data
public class AssetsDirectoryDTO {

    public String key;

    public String parent;

    public String name;

    public Integer level;

    public Boolean skip;

    public List<String> superTypes;


}
