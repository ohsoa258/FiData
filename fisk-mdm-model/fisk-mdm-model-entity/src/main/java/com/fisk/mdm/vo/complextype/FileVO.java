package com.fisk.mdm.vo.complextype;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author JianWenYang
 */
@Data
public class FileVO {

    private String file_name;

    private String file_path;

    private LocalDateTime create_time;

    private Long create_user;

}
