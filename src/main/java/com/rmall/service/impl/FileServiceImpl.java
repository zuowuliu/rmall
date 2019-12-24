package com.rmall.service.impl;

import com.google.common.collect.Lists;
import com.rmall.service.IFileService;
import com.rmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @author 大神爱吃茶
 * @Date 2019/12/6 0006 下午 21:45
 */
@Service
public class FileServiceImpl implements IFileService {

    //由于这个服务经常会被调用,所以需要打印一下日志的信息
    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    public String upload(MultipartFile file, String path){

        //getOriginalFilename是MultipartFile file里的方法,将文件的名字获取出来
        String fileName = file.getOriginalFilename();

        //文件的扩展名
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);

        //定义上传的文件的名字(为了避免不同的用户上传了相同的文件名而导致报错)
        String uploadFileName = UUID.randomUUID().toString()+"."+fileExtensionName;

        //通过{}进行占位
        logger.info("开始上传文件,上传文件的文件名:{},上传的路径:{},新文件名:{}",fileName,path,uploadFileName);

        //声明目录的file，根据声明的file的path来创建一个新的file
        //File用于操作文件和文件夹(目录)
        File fileDir = new File(path);
        if(!fileDir.exists()){
            fileDir.setReadable(true);
            fileDir.mkdirs();
        }
        //这是最终生成文件的详细信息，包含生成文件的路径位置及最终文件的访问名字。保证能够访问到
        File targetFile = new File(path,uploadFileName);
            try {

            //通过transferTo方法上传成功
            file.transferTo(targetFile);

            //将targetFile上传到我们的FTP服务器上
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));

            //上传完之后，删除upload下面的文件
            targetFile.delete();


        } catch (IOException e) {
            logger.error("上传文件异常", e);
        }
        return targetFile.getName();
    }
}
