package com.zklcsoftware.aimodel.util;

import com.zklcsoftware.basic.repository.DynamicSqlException;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class TemplateService {

    protected static Configuration freeMarkerEngine = new Configuration();
    static {
        freeMarkerEngine.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);
    }
    /**
     * 根据模板内容和参数生成文本
     *
     * @param templateContent 模板内容（字符串形式）
     * @param model           参数集合
     * @return 生成的文本
     */
    public String generateText(String templateContent, Map<String, Object> model) {
        StringWriter out = new StringWriter();
        try {
            // 将模板内容加载到Configuration中
            Template tpl = new Template("tpl", new StringReader(templateContent), freeMarkerEngine);
            tpl.process(model, out);
            templateContent=out.toString();
        } catch (IOException | TemplateException e) {
            throw new RuntimeException("模板解析失败", e);
        }
        return out.toString();
    }
}
