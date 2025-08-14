package com.zklcsoftware.aimodel.service;

import java.util.List;
import com.zklcsoftware.basic.service.BaseService;
import com.zklcsoftware.aimodel.domain.TSchoolcloudSysDictionary;

public interface TSchoolcloudSysDictionaryService extends BaseService<TSchoolcloudSysDictionary,Integer> {

	public List<TSchoolcloudSysDictionary> queryDictList(Integer dictType);
}
