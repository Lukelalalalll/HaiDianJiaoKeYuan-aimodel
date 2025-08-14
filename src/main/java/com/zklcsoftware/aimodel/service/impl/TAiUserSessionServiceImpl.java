package com.zklcsoftware.aimodel.service.impl;

import com.zklcsoftware.aimodel.domain.TAiModel;
import com.zklcsoftware.aimodel.domain.TAiSysPrompt;
import com.zklcsoftware.aimodel.domain.TAiUserSessionModel;
import com.zklcsoftware.aimodel.dto.SessionlQueryDTO;
import com.zklcsoftware.aimodel.dto.TAiModelLayoutItemDTO;
import com.zklcsoftware.aimodel.dto.TAiUserSessionDTO;
import com.zklcsoftware.aimodel.repository.*;
import com.zklcsoftware.aimodel.service.TAiModelService;
import com.zklcsoftware.aimodel.service.TAiSysPromptService;
import com.zklcsoftware.aimodel.util.ConstantUtil;
import com.zklcsoftware.aimodel.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zklcsoftware.basic.service.impl.BaseServiceImpl;
import com.zklcsoftware.aimodel.domain.TAiUserSession;
import com.zklcsoftware.aimodel.service.TAiUserSessionService;

import java.util.*;

@Service
@Transactional
public class TAiUserSessionServiceImpl extends BaseServiceImpl<TAiUserSession, String> implements TAiUserSessionService {
    @Autowired
    TAiUserSessionRepository tAiUserSessionRepository;
    @Autowired
    TAiUserSessionModelRepository tAiUserSessionModelRepository;
    @Autowired
    TAiUserSessionModelContextRepository tAiUserSessionModelContextRepository;
    @Autowired
    TAiSysPromptService tAiSysPromptService;
    @Autowired
    TAiModelService tAiModelService;
    @Autowired
    private TAiUserSessionModelContextFileindexRepository tAiUserSessionModelContextFileindexRepository;
    @Autowired
    TAiModelLayoutItemRepository aiModelLayoutItemRepository;

    @Override
    public List<TAiUserSessionVO> queryAiUserSessions(SessionlQueryDTO sessionlQueryVO, String userId) {
        List<TAiUserSessionVO> reList=new ArrayList<>();
        List<TAiUserSession> dataList=null;
        if(sessionlQueryVO!=null && sessionlQueryVO.getSessionType()== ConstantUtil.SESSION_TYPE_1){
            if(sessionlQueryVO.getLastNum()!=null){
                dataList=tAiUserSessionRepository.findFirst3ByUserIdAndIsDelAndSysPromptIdIsNullOrderByUpdateTimeDesc(userId,ConstantUtil.IS_DEL_0);
            }else{
                dataList=tAiUserSessionRepository.findByUserIdAndIsDelAndSysPromptIdIsNullOrderByUpdateTimeDesc(userId,ConstantUtil.IS_DEL_0);
            }

            if(dataList!=null){
                TAiUserSessionVO tAiUserSessionlVO=null;
                for (TAiUserSession tAiUserSession : dataList) {
                    tAiUserSessionlVO=new TAiUserSessionVO();
                    BeanUtils.copyProperties(tAiUserSession,tAiUserSessionlVO);
                    List<TAiUserSessionModel> tAiUserSessionModels=tAiUserSessionModelRepository.findBySessionId(tAiUserSessionlVO.getId());
                    if(tAiUserSessionModels!=null){
                        for (TAiUserSessionModel tAiUserSessionModel : tAiUserSessionModels) {
                            TAiModel tAiModel=tAiModelService.findById(tAiUserSessionModel.getModelId());
                            tAiUserSessionlVO.getTAiUserSessionlModels()
                                    .add(TAiUserSessionModelVO.builder()
                                            .modelId(tAiModel.getId())
                                            .modelName(tAiModel.getName())
                                            .modelImg(tAiModel.getProImg())
                                            .id(tAiUserSessionModel.getId()).build());
                        }
                    }
                    reList.add(tAiUserSessionlVO);
                }
            }

        }else if(sessionlQueryVO!=null && sessionlQueryVO.getSessionType()== ConstantUtil.SESSION_TYPE_2){
            dataList=tAiUserSessionRepository.findByUserIdAndIsDelAndSysPromptIdIsNotNullOrderByCreateTimeDesc(userId,ConstantUtil.IS_DEL_0);

            if(dataList!=null){
                TAiUserSessionVO tAiUserSessionlVO=null;
                for (TAiUserSession tAiUserSession : dataList) {
                    TAiSysPrompt tAiSysPrompt=tAiSysPromptService.findById(tAiUserSession.getSysPromptId());

                    if((tAiSysPrompt!=null && !tAiSysPrompt.getCreateUser().equals(userId)) && tAiSysPrompt.getSysPromptType()!=ConstantUtil.SYS_PROMPT_TYPE_1 && tAiSysPrompt.getSysPromptType()!=ConstantUtil.SYS_PROMPT_TYPE_3){
                        if(tAiSysPrompt.getIsDel()!=1){
                            tAiUserSessionlVO=new TAiUserSessionVO();
                            BeanUtils.copyProperties(tAiUserSession,tAiUserSessionlVO);
                            tAiUserSessionlVO.setProImg(tAiSysPrompt.getProImg());
                            tAiUserSessionlVO.setName(tAiSysPrompt.getName());
                            tAiUserSessionlVO.setIsUseSzr(tAiSysPrompt.getIsUseSzr());
                            Long twcs=tAiUserSessionModelContextRepository.countContextBySysPromptId(tAiSysPrompt.getId());//统计智能体提问数
                            tAiUserSessionlVO.setTwcs(twcs);
                            reList.add(tAiUserSessionlVO);
                        }
                    }
                }
            }

        }else if(sessionlQueryVO!=null && sessionlQueryVO.getSessionType()== ConstantUtil.SESSION_TYPE_3){
            dataList=tAiUserSessionRepository.findByUserIdAndIsDelAndSysPromptIdIsNotNullOrderByCreateTimeDesc(userId,ConstantUtil.IS_DEL_0);

            if(dataList!=null){
                TAiUserSessionVO tAiUserSessionlVO=null;
                for (TAiUserSession tAiUserSession : dataList) {
                    TAiSysPrompt tAiSysPrompt=tAiSysPromptService.findById(tAiUserSession.getSysPromptId());
                    if(tAiSysPrompt!=null && tAiSysPrompt.getIsDel()!=1 && tAiSysPrompt.getCreateUser().equals(userId) && tAiSysPrompt.getSysPromptType()==ConstantUtil.SYS_PROMPT_TYPE_2){
                        //如果是本人回显
                        tAiUserSessionlVO=new TAiUserSessionVO();
                        BeanUtils.copyProperties(tAiUserSession,tAiUserSessionlVO);

                        //我创建的智能体添加状态属性
                        tAiUserSessionlVO.setReviewStatus(String.valueOf(tAiSysPrompt.getReviewStatus()));
                        tAiUserSessionlVO.setProImg(tAiSysPrompt.getProImg());
                        tAiUserSessionlVO.setName(tAiSysPrompt.getName());
                        tAiUserSessionlVO.setIsUseSzr(tAiSysPrompt.getIsUseSzr());

                        Long twcs=tAiUserSessionModelContextRepository.countContextBySysPromptId(tAiSysPrompt.getId());//统计智能体提问数
                        tAiUserSessionlVO.setTwcs(twcs);

                        reList.add(tAiUserSessionlVO);
                    }
                }
            }

        }else if(sessionlQueryVO!=null && sessionlQueryVO.getSessionType()== ConstantUtil.SESSION_TYPE_4
                && (StringUtils.isNotBlank(sessionlQueryVO.getSysPromptId()) || (sessionlQueryVO.getSysPromptIds()!=null && sessionlQueryVO.getSysPromptIds().size()>0))){
            /*if(sessionlQueryVO.getLastNum()!=null){
                dataList=tAiUserSessionRepository.findFirst3ByUserIdAndIsDelAndSysPromptIdOrderByUpdateTimeDesc(userId,ConstantUtil.IS_DEL_0,sessionlQueryVO.getSysPromptId());
            }else{
                dataList=tAiUserSessionRepository.findByUserIdAndIsDelAndSysPromptIdOrderByUpdateTimeDesc(userId,ConstantUtil.IS_DEL_0,sessionlQueryVO.getSysPromptId());
            }*/

            dataList=tAiUserSessionRepository.queryUserAiSession(userId,ConstantUtil.IS_DEL_0,sessionlQueryVO);

            if(dataList!=null){
                TAiUserSessionVO tAiUserSessionlVO=null;
                for (TAiUserSession tAiUserSession : dataList) {
                    tAiUserSessionlVO=new TAiUserSessionVO();
                    BeanUtils.copyProperties(tAiUserSession,tAiUserSessionlVO);

                    List<TAiUserSessionModel> tAiUserSessionModels=tAiUserSessionModelRepository.findBySessionId(tAiUserSessionlVO.getId());
                    if(tAiUserSessionModels!=null && tAiUserSessionModels.size()>0){
                        tAiUserSessionlVO.getTAiUserSessionlModels()
                                .add(TAiUserSessionModelVO.builder()
                                        .modelId(tAiUserSessionModels.get(0).getModelId())
                                        .id(tAiUserSessionModels.get(0).getId()).build());
                    }
                    reList.add(tAiUserSessionlVO);
                }
            }

        }

        return reList;
    }

    @Override
    public TAiUserSessionVO createAiSession(TAiUserSessionDTO tAiUserSessionlDTO, String userId,String userType,String userName,String chName,String studentId) {

        TAiUserSessionVO tAiUserSessionVO=new TAiUserSessionVO();
        TAiUserSession tAiUserSession=null;
        if(StringUtils.isNotBlank(tAiUserSessionlDTO.getSysPromptId())){
            TAiSysPrompt tAiSysPrompt=tAiSysPromptService.findById(tAiUserSessionlDTO.getSysPromptId());
            if(tAiSysPrompt.getSysPromptType()!=1){//类型 1-智能体应用  2-用户智能体 3-工具智能体
                tAiUserSession=tAiUserSessionRepository.findByUserIdAndSysPromptIdAndIsDel(userId,tAiUserSessionlDTO.getSysPromptId(),ConstantUtil.IS_DEL_0);
            }
        }

        //判断该用户是否已存在该智能体的会话
        if(tAiUserSession!=null){
            tAiUserSessionVO.setId(tAiUserSession.getId());
            tAiUserSessionVO.setSysPromptId(tAiUserSession.getSysPromptId());
            tAiUserSessionVO.setUserPromptId(tAiUserSession.getUserPromptId());
            if(StringUtils.isNotBlank(tAiUserSessionlDTO.getSysPromptId())){
                List<TAiUserSessionModel> tAiUserSessionModels=tAiUserSessionModelRepository.findBySessionId(tAiUserSession.getId());
                if(tAiUserSessionModels!=null && tAiUserSessionModels.size()>0){
                    tAiUserSessionVO.getTAiUserSessionlModels()
                            .add(TAiUserSessionModelVO.builder()
                                    .modelId(tAiUserSessionModels.get(0).getModelId())
                                    .id(tAiUserSessionModels.get(0).getId()).build());
                }
            }
        }else{
            tAiUserSession=
                    TAiUserSession.builder().name(tAiUserSessionlDTO.getName())
                            .userId(userId)
                            .isDel(ConstantUtil.IS_DEL_0)
                            .userType(userType!=null?Integer.parseInt(userType):0)
                            .userName(userName)
                            .chName(chName)
                            .sysPromptId(StringUtils.isNotBlank(tAiUserSessionlDTO.getSysPromptId())?tAiUserSessionlDTO.getSysPromptId():null)
                            .userPromptId(tAiUserSessionlDTO.getUserPromptId())
                            .refSessionId(tAiUserSessionlDTO.getRefSessionId())//关联会话ID
                            .studentId(studentId)//对应的学生ID
                            .createTime(new Date())
                            .updateTime(new Date())
                            .build();

            this.save(tAiUserSession);
            tAiUserSessionVO.setId(tAiUserSession.getId());
            tAiUserSessionVO.setSysPromptId(tAiUserSession.getSysPromptId());
            tAiUserSessionVO.setUserPromptId(tAiUserSession.getUserPromptId());
            //判断是对比AI对话还是智能体
            if(tAiUserSessionlDTO!=null){
                String modelId=null;
                if(StringUtils.isNotBlank(tAiUserSessionlDTO.getSysPromptId())){
                    TAiSysPrompt tAiSysPrompt=tAiSysPromptService.findById(tAiUserSessionlDTO.getSysPromptId());
                    modelId=tAiSysPrompt.getModelId();//模型ID
                    TAiUserSessionModel tAiUserSessionModel=TAiUserSessionModel
                            .builder()
                            .sessionId(tAiUserSession.getId())
                            .createTime(tAiUserSession.getCreateTime())
                            .modelId(modelId)
                            .build();
                    if(tAiSysPrompt.getSysPromptType()==2){//类型 1-智能体应用  2-用户智能体 3-工具智能体
                        tAiUserSession.setName(tAiSysPrompt.getName());//智能体会话 名称统一使用智能体名词
                    }
                    this.save(tAiUserSession);
                    tAiUserSessionModelRepository.save(tAiUserSessionModel);
                    tAiUserSessionVO.getTAiUserSessionlModels()
                            .add(TAiUserSessionModelVO.builder()
                                    .modelId(modelId)
                                    .id(tAiUserSessionModel.getId()).build());
                }else {
                    if(tAiUserSessionlDTO.getModelIds()!=null)
                        for (String mid : tAiUserSessionlDTO.getModelIds()) {
                            modelId=mid;
                            TAiUserSessionModel tAiUserSessionModel=TAiUserSessionModel
                                    .builder()
                                    .sessionId(tAiUserSession.getId())
                                    .createTime(tAiUserSession.getCreateTime())
                                    .modelId(modelId)
                                    .build();
                            tAiUserSessionModelRepository.save(tAiUserSessionModel);
                            tAiUserSessionVO.getTAiUserSessionlModels()
                                    .add(TAiUserSessionModelVO.builder()
                                            .modelId(modelId)
                                            .id(tAiUserSessionModel.getId()).build());
                        }
                }
            }
        }
        return tAiUserSessionVO;
    }

    @Override
    public List<TAiUserSessionModelVO>  queryAiUserSessionDetails(String aiSessionId) {
        List<TAiUserSessionModelVO> tAiUserSessionModelVOS=new ArrayList<>();
        List<TAiUserSessionModel> tAiUserSessionModels= tAiUserSessionModelRepository.findBySessionId(aiSessionId);
        //查询知识库文件索引信息
        Map params = new HashMap();
        params.put("aiSessionId",aiSessionId);
        List<TAiUserSessionModelContextFileindexVO> fileindexVOS = tAiUserSessionModelContextFileindexRepository.queryFileIndexBySessionModelId(params);
        String netdiskUrl=ConstantUtil.sysConfig.get("netdisk_api");//获取网盘地址

        for (TAiUserSessionModel tAiUserSessionModel : tAiUserSessionModels) {

            List<TAiUserSessionModelContextVO> contextVOS=tAiUserSessionModelContextRepository.querySessionModelContextBySessionModelId(tAiUserSessionModel.getId());
            //获取索引文件下载地址
            for(TAiUserSessionModelContextVO tAiUserSessionModelContextVO:contextVOS){

                List<TAiUserSessionModelContextFileindexVO> list = new ArrayList<>();
                for(TAiUserSessionModelContextFileindexVO fileindexVo : fileindexVOS){
                    if(tAiUserSessionModelContextVO.getId().equals(fileindexVo.getSessionContextId())){
                        if(ConstantUtil.sysConfig.containsKey("zsk_doc_view_type") && "1".equals(ConstantUtil.sysConfig.get("zsk_doc_view_type"))){//知识库文档查看方式 0下载 1在线预览  默认0
                            fileindexVo.setNetdiskUrl(netdiskUrl + "/download?fileid=" + fileindexVo.getNetdiskId()+"&flag=view");
                        }else{
                            fileindexVo.setNetdiskUrl(netdiskUrl + "/download?fileid=" + fileindexVo.getNetdiskId());
                        }

                        list.add(fileindexVo);
                    }
                }
                tAiUserSessionModelContextVO.setFileindexList(list);
            }

            TAiUserSession tAiUserSession=tAiUserSessionRepository.getOne(tAiUserSessionModel.getSessionId());

            if(tAiUserSession.getSysPromptId()!=null){
                TAiSysPrompt tAiSysPrompt=tAiSysPromptService.findById(tAiUserSession.getSysPromptId());
                //如果从未有关会话记录 则使用智能体的开场白
                if(StringUtils.isNotBlank(tAiSysPrompt.getPrologue())){
                    contextVOS.add(0,TAiUserSessionModelContextVO.builder()
                            .assistantOut(tAiSysPrompt.getPrologue()).build());
                }
            }

            tAiUserSessionModelVOS.add(TAiUserSessionModelVO.builder()
                     .id(tAiUserSessionModel.getId())
                     .contextList(contextVOS)
                    .modelId(tAiUserSessionModel.getModelId()).build());

        }
        return tAiUserSessionModelVOS;
    }

    @Override
    public List<TJxxVO> queryUsrTypeTjxxList(Integer userType,Integer year) {
        Map params=new HashMap<>();
        params.put("userType",userType);
        params.put("year",year);
        return tAiUserSessionRepository.findListObj("queryUsrTypeTjxxList",params,TJxxVO.class);
    }

    @Override
    public Page<TJxxVO> queryTwxqxxList(String startDate, String endDate, Integer userType, Pageable pageable) {
        Map params=new HashMap<>();
        if(StringUtils.isNotBlank(startDate)){
            params.put("startDate",startDate+" 00:00:00");
        }
        if(StringUtils.isNotBlank(endDate)){
            params.put("startDate",startDate+" 23:59:59");
        }
        params.put("userType",userType);
        return tAiUserSessionRepository.findPage("queryTwxqxxList","queryTwxqxxList_count",params,pageable,TJxxVO.class);
    }

    @Override
    public List<TAiSysPromptVO> queryHotTopListForZnt(int topNum,Integer userType) {
        Map params=new HashMap();
        params.put("topNum",topNum);
        params.put("userType",userType);

        return  tAiUserSessionRepository.findListObj("queryHotTopListForZnt",params,TAiSysPromptVO.class);
    }

    @Override
    public List<TAiUserSessionModelContextKeysVO> queryHotTopListForQuestion(int topNum,Integer userType) {
        Map params=new HashMap();
        params.put("topNum",topNum);
        params.put("userType",userType);
        return  tAiUserSessionRepository.findListObj("queryHotTopListForQuestion",params,TAiUserSessionModelContextKeysVO.class);
    }

    @Override
    public Page<TAiUserSessionVO> queryAllAiUserSessionsPage(String layoutId, String userId, Pageable pageable) {
        List<TAiModelLayoutItemDTO> itemList = aiModelLayoutItemRepository.getItemsByLayoutIdOrderbySort(layoutId);
        List<String> sysPromptIds=new ArrayList<>();
        for(TAiModelLayoutItemDTO item:itemList){
            if(StringUtils.isNotBlank(item.getSysPromptId())){
                sysPromptIds.add(item.getSysPromptId());
            }
        }

        Map params=new HashMap();
        params.put("sysPromptIds",sysPromptIds);
        params.put("userId",userId);
        return tAiUserSessionRepository.findPage("queryAllAiUserSessionsPage", "queryAllAiUserSessionsPageCount", params, pageable, TAiUserSessionVO.class);
    }
}
