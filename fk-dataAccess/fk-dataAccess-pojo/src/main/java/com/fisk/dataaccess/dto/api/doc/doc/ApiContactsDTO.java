package com.fisk.dataaccess.dto.api.doc.doc;

import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 联系人
 * @date 2022/2/3 14:13
 */
@Data
public class ApiContactsDTO {
    /**
     * 类别
     */
    public String category;

    /**
     * 所属公司/部门
     */
    public String company;

    /**
     * 姓名
     */
    public String fullName;

    /**
     * 联系方式，邮箱地址
     */
    public String mailbox;

    /**
     * 行样式
     */
    public String trStyle;
}
