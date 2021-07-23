package com.fisk.datamodel.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class ProjectDimensionSourceDTO {
    @TableId
    public long id;
    public String dimensionCnName;
    public List<ProjectDimensionDTO> data;
}