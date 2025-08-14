package com.zklcsoftware.aimodel.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.zklcsoftware.aimodel.repository.TShuzirenServiceRepository;
import com.zklcsoftware.aimodel.util.ConstantUtil;
import com.zklcsoftware.common.dto.OperaResult;
import com.zklcsoftware.common.web.util.HttpClients;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zklcsoftware.basic.service.impl.BaseServiceImpl;
import com.zklcsoftware.aimodel.domain.TShuzirenService;
import com.zklcsoftware.aimodel.service.TShuzirenServiceService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TShuzirenServiceServiceImpl extends BaseServiceImpl<TShuzirenService, Integer> implements TShuzirenServiceService {

    @Autowired
    private TShuzirenServiceRepository tShuzirenServiceRepository;

    @Override
    public List<TShuzirenService> findByStatus(Integer status) {
        return tShuzirenServiceRepository.findByStatus(status);
    }

    @Override
    public TShuzirenService getMinServiceIp() {
        //获取数字人token
        String token = ConstantUtil.sysConfig.get("digital_person_token");
        //查询已启用的服务器
        List<TShuzirenService> serviceList = this.findByStatus(1);

        int minNum = 0;//服务器最小任务数
        TShuzirenService tShuzirenService = null;
        for(int i=0; i<serviceList.size(); i++){

            int count = 0;//统计服务使用状态

            try {
                String url = "http://"+ serviceList.get(i).getServiceAddress() +":5000/v1/status";
                Map dataMap=new HashMap();
                dataMap.put("token", token);
                String result = HttpClients.get(url, dataMap);
                if(StringUtils.isNotEmpty(result)){
                    JSONObject jsonObject = JSONObject.parseObject(result);
                    count = (int) jsonObject.get("time");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //将第一个ip使用数作为最小数
            if(i == 0){
                minNum = count;
                tShuzirenService = serviceList.get(i);
            }

            //使用数小于最小数，则替换
            if(count < minNum){
                minNum = count;
                tShuzirenService = serviceList.get(i);
            }

            //当队列数为0时直接使用该服务器，跳出循环
            if(count == 0){
                break;
            }
        }
        return tShuzirenService;
    }
}
