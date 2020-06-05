package com.sheldon.factory.file;

import io.netty.handler.codec.string.LineSeparator;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * @Description: 文件初始化器
 * @Author: SheldonPeng
 * @Date: 2020-06-03 09:55
 */
@Component
@Log4j2
public class FileFactory {


    @Value("${ftp.root}")
    private String root;

    private String rootPath;
    /**
     * @Description: 初始化FTP服务器文件目录
     * @Param: []
     * @Return: void
     * @Author: SheldonPeng
     * @Date: 2020/6/3  09:57
     */
    @PostConstruct
    public void init() {

        // 当前ftp根目录
        String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath() + root;
        File file = new File(path);
        // 检测文件目录是否存在以及当前文件是否为文件夹
        if (!(file.exists() && file.isDirectory())) {
            // 创建文件失败
            if (! file.mkdir()){
                throw new RuntimeException("FTP Server Start Fail!");
            }
        }
        setRootPath(path);
        log.info("FTP root目录初始化成功！");
    }


    private void setRootPath(String rootPath){
        this.rootPath = rootPath;
    }

    public String getSimpleInfo(String path){

        File file = new File(path);
        File[] files = file.listFiles();
        StringBuilder stringBuilder = new StringBuilder();
        for (File childFile : files) {
            stringBuilder.append(childFile.getName());
            stringBuilder.append(LineSeparator.WINDOWS.value());
        }
        return stringBuilder.toString();
    }


    public String getDetailInfo(String path) throws IOException {
        File file = new File(path);
        File[] files = file.listFiles();
        StringBuilder stringBuilder = new StringBuilder();
        for (File childFile : files) {

            Path childPath = Paths.get(childFile.toURI());
            BasicFileAttributeView fileAttributeView = Files.getFileAttributeView(childPath, BasicFileAttributeView.class);
            BasicFileAttributes basicFileAttributes = fileAttributeView.readAttributes();
            LocalDateTime localDateTime = LocalDateTime.ofInstant(basicFileAttributes.lastModifiedTime().toInstant(), ZoneId.systemDefault());
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd-yy  HH:mma", Locale.ENGLISH);
            stringBuilder.append(localDateTime.format(dateTimeFormatter));
            stringBuilder.append("       ");
            if ( basicFileAttributes.isDirectory()){
                stringBuilder.append("<DIR>");
                stringBuilder.append("\t\t");
            }else {
                stringBuilder.append("     ");
                stringBuilder.append("\t\t");
                stringBuilder.append(basicFileAttributes.size());
            }
            stringBuilder.append("\t\t");
            stringBuilder.append(childFile.getName());
            stringBuilder.append(LineSeparator.WINDOWS.value());
        }
        return stringBuilder.toString();
    }

    public String getRootPath(){
        return this.rootPath;
    }
}