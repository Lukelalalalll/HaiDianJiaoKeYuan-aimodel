package com.zklcsoftware.aimodel.service;

import java.util.List;
import com.zklcsoftware.basic.service.BaseService;
import com.zklcsoftware.aimodel.domain.TAiAttachment;
import com.zklcsoftware.common.dto.OperaResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface TAiAttachmentService extends BaseService<TAiAttachment,String> {

    /**
     * 查询知识库分页
     * @param zntId
     * @param pageable
     * @return
     */
    Page<TAiAttachment> queryZskPage(String zntId, Pageable pageable);

    /**
     * 查询知识库列表
     * @param zntId
     * @return
     */
    List<TAiAttachment> queryZskList(String zntId);

    /**
     * 删除知识库
     * @param attachmentId
     * @return
     */
    OperaResult delZsk(String attachmentId);

    /**
     * 知识库上传
     * @param multipartFile
     * @param zntId
     * @param userGuid
     * @param uName
     * @return
     */
    OperaResult zskUpload(MultipartFile multipartFile, String zntId, String userGuid, String uName);

    /**
     * 知识库压缩包上传
     * @param multipartFile
     * @param zntId
     * @param userGuid
     * @param uName
     * @return
     */
    OperaResult zskZipUpload(MultipartFile multipartFile, String zntId, String userGuid, String uName);
}
