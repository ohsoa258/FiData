package com.fisk.common.core.enums.factory;

import com.fisk.common.core.enums.BaseEnum;

public enum TaskSettingEnum implements BaseEnum {
    /**
     * sftp任务配置-数据源-ip
     */
    sftp_source_ip(1, "sftp任务配置-数据源-ip", "sftpSourceIp"),
    /**
     * sftp任务配置-数据源-身份验证类型
     */
    sftp_source_authentication_type(2, "sftp任务配置-数据源-身份验证类型", "sftpSourceAuthenticationType"),
    /**
     * sftp任务配置-数据源-账户
     */
    sftp_source_account(3, "sftp任务配置-数据源-账户", "sftpSourceAccount"),
    /**
     * sftp任务配置-数据源-密码
     */
    sftp_source_password(4, "sftp任务配置-数据源-密码", "sftpSourcePassword"),
    /**
     * sftp任务配置-数据源-目录
     */
    sftp_source_folder(5, "sftp任务配置-数据源-目录", "sftpSourceFolder"),
    /**
     * sftp任务配置-数据源-排序方式
     */
    sftp_source_sortord(6, "sftp任务配置-数据源-排序方式", "sftpSourceSortord"),
    /**
     * sftp任务配置-数据源-规则
     */
    sftp_source_ordering_rule(7, "sftp任务配置-数据源-规则", "sftpSourceOrderingRule"),
    /**
     * sftp任务配置-数据源-取第几个
     */
    sftp_source_number(8, "sftp任务配置-数据源-取第几个", "sftpSourceNumber"),

    /**
     * sftp任务配置-目标-ip
     */
    sftp_target_ip(9, "sftp任务配置-目标-ip", "sftpTargetIp"),
    /**
     * sftp任务配置-目标-身份验证类型
     */
    sftp_target_authentication_type(10, "sftp任务配置-目标-身份验证类型", "sftpTargetAuthenticationType"),
    /**
     * sftp任务配置-目标-账户
     */
    sftp_target_account(11, "sftp任务配置-目标-账户", "sftpTargetAccount"),
    /**
     * sftp任务配置-目标-密码
     */
    sftp_target_password(12, "sftp任务配置-目标-密码", "sftpTargetPassword"),
    /**
     * sftp任务配置-目标-目录
     */
    sftp_target_folder(13, "sftp任务配置-目标-目录", "sftpTargetFolder"),
    /**
     * sftp任务配置-目标-文件名称
     */
    sftp_target_file_name(14, "sftp任务配置-目标-文件名称", "sftpTargetFileName"),
    /**
     * sftp源rsa文件路径
     */
    sftp_source_rsa_file_path(15, "sftp任务配置-数据源-rsa文件路径", "sftpSourceRsaFilePath"),
    /**
     * sftp目标rsa文件路径
     */
    sftp_target_rsa_file_path(16, "sftp任务配置-目标-rsa文件路径", "sftpTargetRsaFilePath");

    private final String name;

    private final String attributeName;

    private final int value;

    TaskSettingEnum(int value, String name, String attributeName) {
        this.name = name;
        this.value = value;
        this.attributeName = attributeName;
    }

    public static TaskSettingEnum getValue(String value) {
        TaskSettingEnum[] timeEnums = values();
        for (TaskSettingEnum typeEnum : timeEnums) {
            String queryValue = typeEnum.getName();
            if (queryValue.equals(value)) {
                return typeEnum;
            }
        }
        return null;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getAttributeName(){return attributeName;}

}
