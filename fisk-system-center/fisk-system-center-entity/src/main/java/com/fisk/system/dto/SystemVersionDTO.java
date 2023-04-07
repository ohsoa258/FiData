package com.fisk.system.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @TableName tb_system_version
 */
@Data
public class SystemVersionDTO {

    /**
     * 版本id
     */
    private Integer id;

    /**
     * 版本号
     */
    private String version;

    /**
     * 发布时间
     */
    private String publishTime;

}
