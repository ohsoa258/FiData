package com.fisk.dataaccess.test;

import com.fisk.common.core.utils.Dto.sftp.SftpExcelTreeDTO;
import com.fisk.common.core.utils.sftp.SftpUtils;
import com.jcraft.jsch.ChannelSftp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author SongJianJian
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class SftpTest {

    @Test
    public void test1(){
        ChannelSftp sftp = SftpUtils.connect("192.168.21.21", 22, "sftp", "password01!", "");

        SftpUtils utils = new SftpUtils();
        SftpExcelTreeDTO list = utils.getFile(sftp, "/", "xlsx");
        System.out.println(list);
    }
}
