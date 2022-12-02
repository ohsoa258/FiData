package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
@TableName("tb_file_service")
public class FileServicePO extends BasePO {

    /**
     * 文件名称
     */
    public String name;

    /**
     * 显示名称
     */
    public String displayName;

    /**
     * 描述
     */
    public String describe;

    /**
     * sql脚本
     */
    public String sqlScript;

    /**
     * 存储文件源id
     */
    public Integer targetSourceId;

    /**
     * 文件存储路径
     */
    public String storagePath;

}
