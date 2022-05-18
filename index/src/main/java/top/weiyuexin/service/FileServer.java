package top.weiyuexin.service;

import org.springframework.web.multipart.MultipartFile;
import top.weiyuexin.entity.vo.R;
import top.weiyuexin.entity.vo.UploadMsg;

import javax.servlet.http.HttpSession;


public interface FileServer {
    /**
     * 图片上传接口
     * @param file
     * @return
     */
    R upload(MultipartFile file, HttpSession session);
    /**
     * 文件上传接口
     * @param file
     * @return
     */
    R uploadFile(MultipartFile file);
}
