package com.fisk.dataaccess.test;

import com.fisk.common.core.utils.Dto.sftp.ExcelTreeDTO;
import com.fisk.common.core.utils.sftp.;
import com.fisk.dataaccess.entity.NifiConfigPO;
import com.fisk.dataaccess.enums.ComponentIdTypeEnum;
import com.fisk.dataaccess.enums.FtpFileTypeEnum;
import com.fisk.dataaccess.mapper.NifiConfigMapper;
import com.fisk.dataaccess.service.impl.NifiConfigImpl;
import com.fisk.task.dto.daconfig.DataSourceConfig;
import com.jcraft.jsch.ChannelSftp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author SongJianJian
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class SftpTest {

//    @Test
//    public void test1(){
//        ChannelSftp sftp = SftpUtils.connect("192.168.21.21", 22, "sftp", "password01!", "");
//
//        SftpUtils utils = new SftpUtils();
//        ExcelTreeDTO list = utils.getFile(sftp, "/", "xlsx");
//        System.out.println(list);
//    }
}
