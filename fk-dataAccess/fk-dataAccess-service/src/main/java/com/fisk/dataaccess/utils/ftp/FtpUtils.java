package com.fisk.dataaccess.utils.ftp;

import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.ftp.ExcelPropertyDTO;
import com.fisk.dataaccess.dto.ftp.ExcelTreeDTO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author : lock
 * @description: Excel parse util.
 * @date : 2021/12/22 13:14
 */
public class FtpUtils {

    private static final String ROOT_PATH = "/";

    /**
     * 连接 FTP 服务器
     *
     * @param addr     FTP 服务器 IP 地址
     * @param port     FTP 服务器端口号
     * @param username 登录用户名
     * @param password 登录密码
     * @return ftpClient ftp客户端
     */
    public static FTPClient connectFtpServer(String addr, int port, String username, String password, String controlEncoding) {
        FTPClient ftpClient = new FTPClient();
        try {
            // 设置文件传输的编码
            ftpClient.setControlEncoding(controlEncoding);

            /*
              连接 FTP 服务器
              如果连接失败，则此时抛出异常，如ftp服务器服务关闭时，抛出异常：
              java.net.ConnectException: Connection refused: connect
              */
            ftpClient.connect(addr, port);
            /*
              登录 FTP 服务器
              1）如果传入的账号为空，则使用匿名登录，此时账号使用 "Anonymous"，密码为空即可
              */
            if (StringUtils.isBlank(username)) {
                ftpClient.login("Anonymous", "");
            } else {
                ftpClient.login(username, password);
            }

            /*
            设置传输的文件类型
              BINARY_FILE_TYPE：二进制文件类型
              ASCII_FILE_TYPE：ASCII传输方式，这是默认的方式
              ....
             */
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

            /*
              确认应答状态码是否正确完成响应
              凡是 2开头的 isPositiveCompletion 都会返回 true，因为它底层判断是：
              return (reply >= 200 && reply < 300);
             */
            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                /*
                  如果 FTP 服务器响应错误 中断传输、断开连接
                  abort：中断文件正在进行的文件传输，成功时返回 true,否则返回 false
                  disconnect：断开与服务器的连接，并恢复默认参数值
                 */
                ftpClient.abort();
                ftpClient.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(">>>>>FTP服务器连接登录失败，请检查连接参数是否正确，或者网络是否通畅*********");
        }
        return ftpClient;
    }

    /**
     * 使用完毕，应该及时关闭连接
     * 终止 ftp 传输
     * 断开 ftp 连接
     *
     * @param ftpClient ftpClient
     * @return 执行结果
     */
    public static FTPClient closeFtpConnect(FTPClient ftpClient) {
        try {
            if (ftpClient != null && ftpClient.isConnected()) {
                ftpClient.abort();
                ftpClient.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ftpClient;
    }

    /**
     * 遍历目录下的所有文件--方式1
     *
     * @param targetDir targetDir
     */
    public static Collection<File> localListFiles(File targetDir) {
        Collection<File> fileCollection = new ArrayList<>();
        if (targetDir != null && targetDir.exists() && targetDir.isDirectory()) {
            /*
              targetDir：不要为 null、不要是文件、不要不存在
              第二个 文件过滤 参数如果为 FalseFileFilter.FALSE ，则不会查询任何文件
              第三个 目录过滤 参数如果为 FalseFileFilter.FALSE , 则只获取目标文件夹下的一级文件，而不会迭代获取子文件夹下的文件
             */
            fileCollection = FileUtils.listFiles(targetDir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        }
        return fileCollection;
    }


    /**
     * 遍历 FTP 服务器指定目录下的所有文件(包含子孙文件)
     *
     * @param ftpClient        ：连接成功有效的 FTP客户端连接
     * @param remotePath       ：查询的 FTP 服务器目录，如果文件，则视为无效，使用绝对路径，如"/"、"/video"、"\\"、"\\video"
     * @param relativePathList ：返回查询结果，其中为服务器目录下的文件相对路径，如：\1.png、\docs\overview-tree.html 等
     * @return 执行结果
     */
    public static List<String> loopServerPath(FTPClient ftpClient, String remotePath, List<String> relativePathList) {
        /*如果 FTP 连接已经关闭，或者连接无效，则直接返回*/
        if (!ftpClient.isConnected() || !ftpClient.isAvailable()) {
            System.out.println("ftp 连接已经关闭或者连接无效......");
            return relativePathList;
        }
        try {
            /*转移到FTP服务器根目录下的指定子目录
              1)"/"：表示用户的根目录，为空时表示不变更
              2)参数必须是目录，当是文件时改变路径无效
              */
            ftpClient.enterLocalPassiveMode();
            ftpClient.changeWorkingDirectory(remotePath);
            /* listFiles：获取FtpClient连接的当前下的一级文件列表(包括子目录)
              1）FTPFile[] ftpFiles = ftpClient.listFiles("/docs/info");
                   获取服务器指定目录下的子文件列表(包括子目录)，以 FTP 登录用户的根目录为基准，与 FTPClient 当前连接目录无关
              2）FTPFile[] ftpFiles = ftpClient.listFiles("/docs/info/springmvc.txt");
                   获取服务器指定文件，此时如果文件存在时，则 FTPFile[] 大小为 1，就是此文件
              */
            FTPFile[] ftpFiles = ftpClient.listFiles();
            if (ftpFiles != null && ftpFiles.length > 0) {
                for (FTPFile ftpFile : ftpFiles) {
                    if (ftpFile.isFile()) {
                        String relativeRemotePath = remotePath + ROOT_PATH + ftpFile.getName();
                        relativePathList.add(relativeRemotePath);
                    } else {
                        loopServerPath(ftpClient, remotePath + ROOT_PATH + ftpFile.getName(), relativePathList);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return relativePathList;
    }

    public static List<String> loopListDirectories(FTPClient ftpClient, String remotePath, List<String> relativePathList) {

        // 如果 FTP 连接已经关闭，或者连接无效，则直接返回
        if (!ftpClient.isConnected() || !ftpClient.isAvailable()) {
            System.out.println("ftp 连接已经关闭或者连接无效......");
            return relativePathList;
        }
        try {
            // 在linux上，由于安全限制，可能某些端口没有开启,此时开启被动本地数据连接
            ftpClient.enterLocalPassiveMode();
            /*
            转移到FTP服务器根目录下的指定子目录
              1)"/"：表示用户的根目录，为空时表示不变更
              2)参数必须是目录，当是文件时改变路径无效
              */
            /*
            listFiles：获取FtpClient连接的当前下的一级文件列表(包括子目录)
              1）FTPFile[] ftpFiles = ftpClient.listFiles("/docs/info");
                   获取服务器指定目录下的子文件列表(包括子目录)，以 FTP 登录用户的根目录为基准，与 FTPClient 当前连接目录无关
              2）FTPFile[] ftpFiles = ftpClient.listFiles("/docs/info/springmvc.txt");
                   获取服务器指定文件，此时如果文件存在时，则 FTPFile[] 大小为 1，就是此文件
              */

            // 读取根目录"/"下面的所有目录
            FTPFile[] ftpFiles = ftpClient.listDirectories();
            if (ftpFiles != null && ftpFiles.length > 0) {
                for (FTPFile ftpFile : ftpFiles) {
                    if (ftpFile.isFile()) {
                        String relativeRemotePath = remotePath + ROOT_PATH + ftpFile.getName();
                        relativePathList.add(relativeRemotePath);
                    } else {
                        loopServerPath(ftpClient, remotePath + ROOT_PATH + ftpFile.getName(), relativePathList);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return relativePathList;

    }

    /**
     * 获取所有文件
     *
     * @param ftpClient  ftpClient
     * @param remotePath remotePath
     * @param fileList   fileList
     * @return 文件列表
     */
    public static List<String> listAllFiles(FTPClient ftpClient, String remotePath, List<String> fileList) {
        ftpClient.enterLocalPassiveMode();
        try {
            if (remotePath.startsWith(ROOT_PATH) && remotePath.endsWith(ROOT_PATH)) {
                // 获取所有文件
                FTPFile[] files = ftpClient.listFiles(remotePath);

                for (FTPFile file : files) {
                    if (file.isFile()) {
                        System.out.println(remotePath);
                        fileList.add(remotePath + file.getName());
                    } else if (file.isDirectory()) {
                        listAllFiles(ftpClient, remotePath + file.getName() + ROOT_PATH, fileList);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileList;
    }


    public static ExcelTreeDTO listFilesAndDirectorys(FTPClient ftpClient, String remotePath, ExcelTreeDTO treeDTO) {
        ftpClient.enterLocalPassiveMode();
        try {

            // 文件
            List<ExcelPropertyDTO> fileList = new ArrayList<>();
            ExcelPropertyDTO filePropertyDto = new ExcelPropertyDTO();
            // 文件夹
            List<ExcelPropertyDTO> directoryList = new ArrayList<>();
            ExcelPropertyDTO dirPropertyDto = new ExcelPropertyDTO();
            // 获取所有文件
            FTPFile[] files = ftpClient.listFiles(remotePath);
            for (FTPFile file : files) {
                if (file.isDirectory()) {
//                    directoryList.add(file.getName());

                } else if (file.isFile()) {
//                    fileList.add(file.getName());
                }
            }

            treeDTO.fileList = fileList;
            treeDTO.directoryList = directoryList;


        } catch (IOException e) {
            throw new FkException(ResultEnum.LOAD_FTP_FILESYSTEM_ERROR);
        }
        return treeDTO;
    }

    /**
     * 返回一个文件流
     *
     * @param fileName fileName
     * @return 文件流
     */
    public String readFile(FTPClient ftpClient, String fileName) {
        String result = "";
        InputStream ins = null;
        try {
            ins = ftpClient.retrieveFileStream(fileName);

            //// byte []b = new byte[ins.available()];
            //// ins.read(b);
            BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
            String inLine = reader.readLine();
            while (inLine != null) {
                result += (inLine + System.getProperty("line.separator"));
                inLine = reader.readLine();
            }
            reader.close();
            if (ins != null) {
                ins.close();
            }

            // 主动调用一次getReply()把接下来的226消费掉. 这样做是可以解决这个返回null问题
            ftpClient.getReply();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 根据名称获取文件，以输入流返回
     *
     * @param ftpPath  FTP服务器上文件相对路径，例如：test/123
     * @param fileName 文件名，例如：test.txt
     * @return InputStream 输入流对象
     */
    public static InputStream getInputStreamByName(FTPClient ftpClient, String ftpPath, String fileName) {
        // 登录
        InputStream input = null;
        if (ftpClient != null) {
            try {
                // 判断是否存在该目录
                if (!ftpClient.changeWorkingDirectory(ftpPath)) {
                    System.out.println(ftpPath + "该目录不存在");
                    return input;
                }
                // 设置被动模式，开通一个端口来传输数据
                ftpClient.enterLocalPassiveMode();
                String[] fs = ftpClient.listNames();
                // 判断该目录下是否有文件
                if (fs == null || fs.length == 0) {
                    return input;
                }
                for (String ff : fs) {
                    String ftpName = new String(ff.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
                    if (ftpName.equals(fileName)) {
                        input = ftpClient.retrieveFileStream(ff);
                        break;
                    }
                }
            } catch (IOException e) {
                System.out.println("获取文件失败" + e);
            } finally {
                closeFtpConnect(ftpClient);
                System.out.println("连接中");
            }
        }
        return input;
    }

////    public static void main(String[] args) {
////        System.out.println("-----------------------应用启动------------------------");
////        FTPClient ftpClient = FTPUtils.connectFtpServer("192.168.1.94", 21, "ftpuser", "password01!", "utf-8");
////        ftpClient.enterLocalPassiveMode();
////
////        List<String> fileList = new ArrayList<>();
////        List<String> directoryList = new ArrayList<>();
////
////        List<String> list = listAllFiles(ftpClient, "/", fileList, directoryList);
////        System.out.println("***************************************");
////
////        list.forEach(System.out::println);
////
////        closeFTPConnect(ftpClient);
////
////        // 获取excel内容
////        InputStream inputStream = getInputStreamByName(ftpClient, "/Windows/二级/", "tb_app_registration.xlsx");
////        List<ExcelDTO> xlsx = ExcelUtils.readExcelFromInputStream(inputStream, ".xlsx");
////        System.out.println("*************************************");
////        System.out.println("文件流对象: ");
////        System.out.println(xlsx);
////
////        System.out.println("-----------------------应用关闭------------------------");
////    }

    public static void main(String[] args) throws Exception {
        System.out.println("-----------------------应用启动------------------------");
        FTPClient ftpClient = FtpUtils.connectFtpServer("192.168.1.94", 21, "ftpuser", "password01!", "utf-8");

        ExcelTreeDTO dto = new ExcelTreeDTO();
        listFilesAndDirectorys(ftpClient, "/Windows/two/三级/", dto);
        System.out.println("dto = " + dto);

        System.out.println("-----------------------应用关闭------------------------");
    }

}