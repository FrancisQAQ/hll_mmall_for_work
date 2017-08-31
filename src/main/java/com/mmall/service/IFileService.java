package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by yao on 2017/8/31.
 */
public interface IFileService {
    String uploadFile(MultipartFile file,String path);
}
