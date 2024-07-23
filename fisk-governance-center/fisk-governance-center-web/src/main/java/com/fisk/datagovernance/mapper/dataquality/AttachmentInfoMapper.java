package com.fisk.datagovernance.mapper.dataquality;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.AttachmentInfoPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

/**
 * @author dick
 * @version 1.0
 * @description 附件Mapper
 * @date 2022/8/15 9:58
 */
@Mapper
public interface AttachmentInfoMapper extends FKBaseMapper<AttachmentInfoPO> {

    /**
     * 新增一条数据并返回生成的主键id
     *
     * @return 执行结果
     */
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("INSERT INTO tb_attachment_info(`original_name`, `current_file_name`, `extension_name`, `relative_path`, `absolute_path`, `category`, `object_id`, `create_time`, `create_user`, `del_flag`) VALUES (#{originalName}, #{currentFileName}, #{extensionName}, #{relativePath}, #{absolutePath}, #{category}, #{objectId},  #{createTime}, #{createUser}, 1);")
    int insertOne(AttachmentInfoPO po);

}
