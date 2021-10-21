package com.fisk.datagovern.dto.label;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class LabelDTO {
    public int id;
    public String labelCnName;
    public int categoryId;
    public String labelEnName;
    public String labelDes;
    /**
     * 应用模块id集合
     */
    public List<String> moduleIds;
    public String applicationModule;
}
