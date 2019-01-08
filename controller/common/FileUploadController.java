package com.cb.controller.common;

import com.cb.common.util.*;
import com.cb.model.active.Record;
import com.cb.model.common.ImageDatabase;
import com.cb.service.activition.RecordService;
import com.cb.service.common.ImageDatabaseService;
import com.cb.vo.ResultVo;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.request.UploadFileRequest;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;

@Controller
@RequestMapping("/common/upload")
public class FileUploadController extends com.cb.common.core.controller.CommonController {

    @Autowired
    private ImageDatabaseService imageDatabaseService;

    @Autowired
    private RecordService recordService;

    /**
     * 请求上传照片
     *
     * @param file    上传的文件
     * @param imgPath 图片上传路径
     * @return 返回上传结果
     */
    @RequestMapping("/img")
    @ResponseBody
    public String uploadImage(MultipartFile file, String imgPath) {
        ResultVo result = new ResultVo();
        try {
            FileUtils.copyToFile(file.getInputStream(), new File(PropertiesUtil.getPropertiesValue("uploadurl") + "temp.jpg"));
            COSClient cosClient = new COSClient(10053351, "AKIDRVInwydfzFeDddzzJTScy1IIqhNLNlBr", "6c59ABjODV51fdGOn3uJ1wjFHho83bH3");
            UploadFileRequest uploadFileRequest = new UploadFileRequest("cbmms", "/image/member/idcard/" + DateUtil.dateToString(DateUtil.newInstanceDate(), DateUtil.DateFormatter.FORMAT_YYYYMMDDHHMMSSSSS) + "." + StringUtil.substringAfter(file.getOriginalFilename(), "."), PropertiesUtil.getPropertiesValue("uploadurl") + "temp.jpg");
            String uploadFileRet = cosClient.uploadFile(uploadFileRequest);
            result.success();
            result.put("fileRet", uploadFileRet);
        } catch (Exception e) {
            logger.error("上传失败：{}", e);
            result.setMessage("上传失败{0}", e.getMessage());
        }
        return result.toJsonString();
    }

    /**
     * 请求上传照片
     *
     * @param file 上传的文件
     * @return 返回上传结果
     */
    @RequestMapping("/product/img")
    @ResponseBody
    public String productImage(MultipartFile file, String productId) {
        ResultVo result = new ResultVo();
        try {
            FileUtils.copyToFile(file.getInputStream(), new File(PropertiesUtil.getPropertiesValue("uploadurl") + "temp.jpg"));
            COSClient cosClient = new COSClient(10053351, "AKIDRVInwydfzFeDddzzJTScy1IIqhNLNlBr", "6c59ABjODV51fdGOn3uJ1wjFHho83bH3");
            UploadFileRequest uploadFileRequest = new UploadFileRequest("cbmms", "/image/product/" + DateUtil.dateToString(DateUtil.newInstanceDate(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD) + "/" + DateUtil.dateToString(DateUtil.newInstanceDate(), DateUtil.DateFormatter.FORMAT_YYYYMMDDHHMMSSSSS) + "." + StringUtil.substringAfter(file.getOriginalFilename(), "."), PropertiesUtil.getPropertiesValue("uploadurl") + "temp.jpg");
            String uploadFileRet = cosClient.uploadFile(uploadFileRequest);
            result.success();
            result.put("fileRet", uploadFileRet);
            HashMap<String, Object> hashMap = (HashMap) JsonUtil.fromJson(uploadFileRet);
            HashMap<String, String> hashMap1 = (HashMap) hashMap.get("data");
            ImageDatabase imageDatabase = imageDatabaseService.add(productId, hashMap1.get("access_url"), file.getOriginalFilename(), ImageDatabase.TypeEnum.PRODUCT);
            result.put("imageId", imageDatabase.getId());
        } catch (Exception e) {
            logger.error("上传失败：{}", e);
            result.setMessage("上传失败{0}", e.getMessage());
        }
        return result.toJsonString();
    }

    /**
     * 请求上传照片
     *
     * @param file 上传的文件
     * @return 返回上传结果
     */
    @RequestMapping("/banner/img")
    @ResponseBody
    public String bannerImage(MultipartFile file, String bannerId) {
        ResultVo result = new ResultVo();
        try {
            FileUtils.copyToFile(file.getInputStream(), new File(PropertiesUtil.getPropertiesValue("uploadurl") + "temp.jpg"));
            COSClient cosClient = new COSClient(10053351, "AKIDRVInwydfzFeDddzzJTScy1IIqhNLNlBr", "6c59ABjODV51fdGOn3uJ1wjFHho83bH3");
            UploadFileRequest uploadFileRequest = new UploadFileRequest("cbmms", "/image/banner/" + DateUtil.dateToString(DateUtil.newInstanceDate(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD) + "/" + DateUtil.dateToString(DateUtil.newInstanceDate(), DateUtil.DateFormatter.FORMAT_YYYYMMDDHHMMSSSSS) + "." + StringUtil.substringAfter(file.getOriginalFilename(), "."), PropertiesUtil.getPropertiesValue("uploadurl") + "temp.jpg");
            String uploadFileRet = cosClient.uploadFile(uploadFileRequest);
            result.success();
            result.put("fileRet", uploadFileRet);
            HashMap<String, Object> hashMap = (HashMap) JsonUtil.fromJson(uploadFileRet);
            HashMap<String, String> hashMap1 = (HashMap) hashMap.get("data");
            ImageDatabase imageDatabase = imageDatabaseService.add(bannerId, hashMap1.get("access_url"), file.getOriginalFilename(), ImageDatabase.TypeEnum.BANNER);
            result.put("imageId", imageDatabase.getId());
        } catch (Exception e) {
            logger.error("上传失败：{}", e);
            result.setMessage("上传失败{0}", e.getMessage());
        }
        return result.toJsonString();
    }

    /**
     * 请求上传照片 口红
     *
     * @param file 上传的文件
     * @return 返回上传结果
     */
    @RequestMapping("/record")
    @ResponseBody
    public String recordImage(MultipartFile file, String recordId) {
        ResultVo result = new ResultVo();
        if (DateUtil.newInstanceDate().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_IMG_START), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) >= 0 &&
                DateUtil.newInstanceDate().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_IMG_END), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) <= 0
                ) {
            try {
                Record record = recordService.isExist(ShiroSecurityUtil.getCurrentMemberId());
                if (record != null && Record.StatusEnum.YES.equals(record.getStatus())) {
                    long time = System.currentTimeMillis();
                    logger.info("上传图片信息：{}", file);
                    FileUtils.copyToFile(file.getInputStream(), new File(PropertiesUtil.getPropertiesValue("uploadurl") + time + ".jpg"));
                    COSClient cosClient = new COSClient(10053351, "AKIDRVInwydfzFeDddzzJTScy1IIqhNLNlBr", "6c59ABjODV51fdGOn3uJ1wjFHho83bH3");
                    logger.info("上传服务参数：{}", cosClient);
                    UploadFileRequest uploadFileRequest = new UploadFileRequest("cbmms", "/image/banner/" + DateUtil.dateToString(DateUtil.newInstanceDate(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD) + "/" + DateUtil.dateToString(DateUtil.newInstanceDate(), DateUtil.DateFormatter.FORMAT_YYYYMMDDHHMMSSSSS) + "." + StringUtil.substringAfter(file.getOriginalFilename(), "."), PropertiesUtil.getPropertiesValue("uploadurl") + time + ".jpg");
                    logger.info("上传参数：{}", uploadFileRequest);
                    String uploadFileRet = cosClient.uploadFile(uploadFileRequest);
                    logger.info("上传结果：{}", uploadFileRet);
                    result.success();
                    result.put("fileRet", uploadFileRet);
                    HashMap<String, Object> hashMap = (HashMap) JsonUtil.fromJson(uploadFileRet);
                    HashMap<String, String> hashMap1 = (HashMap) hashMap.get("data");

                    record.setImgUrl(hashMap1.get("access_url"));
                    recordService.save(record);
                } else {
                    result.setMessage("未参与活动");
                }

            } catch (Exception e) {
                logger.error("上传失败：{}", e);
                result.setMessage("上传失败{0}", e.getMessage());
            }
        } else {
            result.setMessage("不在活动时间");
        }
        return result.toJsonString();
    }

    /**
     * 请求保存会员
     *
     * @return 返回保存结果
     */
    @RequestMapping("/deleteimg")
    @ResponseBody
    public String deleteimg(String id) {
        ResultVo result = new ResultVo();
        if (StringUtil.isNotBlank(id)) {
            ImageDatabase image = imageDatabaseService.findById(id);
            if (image != null) {
                try {
                    imageDatabaseService.delete(image);
                    result.success();
                } catch (Exception e) {
                    logger.info("删除图片出错" + e.getMessage());
                }
            } else {
                result.setMessage("图片不存在");
            }
        } else {
            result.setMessage("参数不能为空");
        }
        return result.toJsonString();
    }
}
