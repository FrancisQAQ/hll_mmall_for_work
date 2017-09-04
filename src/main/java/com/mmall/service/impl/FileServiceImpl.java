package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by yao on 2017/8/31.
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService{
    private static Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    public String uploadFile(MultipartFile file, String path){
        String fileName = file.getOriginalFilename();
        String suffixStr = fileName.substring(fileName.lastIndexOf("."));
        String storageFileName = UUID.randomUUID().toString() + suffixStr;
        logger.info("开始上传文件，上传的文件名：{},路径：{}，新文件名：{}",fileName,path,storageFileName);

        File uploadFileDir = new File(path);
        if(!uploadFileDir.exists()){
            uploadFileDir.setWritable(true);
            uploadFileDir.mkdirs();
        }
        File targetFile = new File(uploadFileDir,storageFileName);
        try {
            file.transferTo(targetFile);    //文件已经上传到tomcat服务器上了
            
            //将文件存储到ftp服务器中
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));

            targetFile.delete();
        } catch (IOException e) {
            logger.error("上传文件出错：",e);
            targetFile.delete();
            return null;
        }
        return targetFile.getName();
    }

    public static void main(String[] args) {
        System.out.println(UUID.randomUUID().toString());
    }
}
