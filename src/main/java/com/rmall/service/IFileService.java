package com.rmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author 大神爱吃茶
 * @Date 2019/12/6 0006 下午 21:43
 */
public interface IFileService {
    String upload(MultipartFile file,String path);
}
