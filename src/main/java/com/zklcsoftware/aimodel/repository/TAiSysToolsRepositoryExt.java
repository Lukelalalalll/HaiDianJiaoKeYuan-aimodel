package com.zklcsoftware.aimodel.repository;

import com.zklcsoftware.aimodel.domain.TAiSysTools;
import com.zklcsoftware.basic.repository.BaseRepositoryExt;

import java.util.List;

/**
 * Service Interface:TAiSysTools
 * @author zklcsoftware_projectarchitecture
 * @date Jun 10, 2025
 */
public interface TAiSysToolsRepositoryExt extends BaseRepositoryExt {

    List<TAiSysTools> queryAiSysTools(String sysPromptId);
}