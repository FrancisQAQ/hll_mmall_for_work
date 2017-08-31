package com.mmall.util;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by yao on 2017/8/31.
 */
public class FTPUtil {

    private static Logger logger = LoggerFactory.getLogger(FTPUtil.class);
    private static String ftpIp = PropertiesUtil.getParam("ftp.server.ip");
    private static String ftpUser = PropertiesUtil.getParam("ftp.user");
    private static String ftpPassword = PropertiesUtil.getParam("ftp.pass");

    private String ip;
    private int port;
    private String user;
    private String password;
    private FTPClient ftpClient;

    public FTPUtil(String ip, int port, String user, String password) {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }

    public static boolean uploadFile(List<File> fileList) throws IOException {
        FTPUtil ftpUtil = new FTPUtil(ftpIp,21,ftpUser,ftpPassword);
        logger.info("开始连接FTP服务器");
        boolean uploadResult = ftpUtil.uploadFile("img",fileList);
        logger.info("结束上传，上传结果：{}",uploadResult);
        return uploadResult;
    }


    private boolean uploadFile(String remotePath,List<File> fileList) throws IOException {
        boolean uploadSuccess = false;
        FileInputStream fileInputStream = null;
        //首先链接FTP服务器
        if(ConnectFTPServer(this.ip,this.port,this.user,this.password)){
            try {
                ftpClient.changeWorkingDirectory(remotePath);
                ftpClient.setBufferSize(1024);
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);  //将文件设置为二进制文件，防治中文乱码
                ftpClient.enterLocalPassiveMode();  //被动传输数据的模式，与ftp文件服务器对应
                for(File fileTemp : fileList){
                    fileInputStream = new FileInputStream(fileTemp);
                    ftpClient.storeFile(fileTemp.getName(),fileInputStream);
                }
                uploadSuccess = true;
            } catch (IOException e) {
                logger.error("上传文件异常",e);
                uploadSuccess = false;
            }finally {
                ftpClient.disconnect();
                fileInputStream.close();
            }
        }
        return uploadSuccess;
    }
    private boolean ConnectFTPServer(String ip,int port,String user,String password){
        boolean isSuccess = false;
        ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip);
            isSuccess = ftpClient.login(user,password);
        } catch (IOException e) {
            logger.error("链接FTP服务器异常",e);
        }
        return isSuccess;
    }

}
