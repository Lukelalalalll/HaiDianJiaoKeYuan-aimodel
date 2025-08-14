package com.zklcsoftware.aimodel.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.zklcsoftware.aimodel.domain.TAiSysPrompt;
import com.zklcsoftware.aimodel.dto.SessionlQueryDTO;
import com.zklcsoftware.aimodel.dto.TAiUserSessionDTO;
import com.zklcsoftware.aimodel.vo.*;
import com.zklcsoftware.basic.service.BaseService;
import com.zklcsoftware.aimodel.domain.TAiUserSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TAiUserSessionService extends BaseService<TAiUserSession,String> {
    List<TAiUserSessionVO> queryAiUserSessions(SessionlQueryDTO sessionlQueryVO, String userId);
    /***
     * @Description 创建用户会话
     * @Author zhushaog 
     * @param: tAiUserSessionlDTO
     * @param: userIdm
     * @param: userType
     * @param: userName
     * @param: chName
     * @param: studentId 关联学生ID
     * @UpdateTime 2025/6/12 13:47 
     * @return: com.zklcsoftware.aimodel.vo.TAiUserSessionVO
     * @throws 
     */
    TAiUserSessionVO createAiSession(TAiUserSessionDTO tAiUserSessionlDTO, String userIdm,String userType,String userName,String chName,String studentId);

    List<TAiUserSessionModelVO>  queryAiUserSessionDetails(String aiSessionId);

    List<TJxxVO> queryUsrTypeTjxxList(Integer userType,Integer year);

    Page<TJxxVO> queryTwxqxxList(String startDate, String endDate, Integer userType, Pageable pageable);

    List<TAiSysPromptVO> queryHotTopListForZnt(int topNum,Integer userType);

    List<TAiUserSessionModelContextKeysVO> queryHotTopListForQuestion(int topNum,Integer userType);

    Page<TAiUserSessionVO> queryAllAiUserSessionsPage(String layoutId, String userId, Pageable pageable);
}
