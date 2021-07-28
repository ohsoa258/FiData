package com.fisk.chartvisual.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author gy
 */
@Data
public class FolderVO {
    public Long id;
    public Long pid;
    public String name;
    public LocalDateTime createTime;
    public List<FolderVO> child;
}
