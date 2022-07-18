package com.fisk.mdm.vo.complextype;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author JianWenYang
 */
@Data
public class FileVO {

    /**
     * 文件名
     */
    private String file_name;

    /**
     * 文件路径
     */
    private String file_path;

    private LocalDateTime create_time;

    private Long create_user;

    /**
     * 版本id
     */
    private Integer fidata_version_id;

    /**
     * 文件类型：0图片类型、1文件类型
     */
    private Integer file_type;

    /**
     * 唯一编码
     */
    private String code;

}
