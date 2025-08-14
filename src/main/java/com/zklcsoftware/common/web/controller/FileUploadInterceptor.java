package com.zklcsoftware.common.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import com.zklcsoftware.basic.util.StringUtil;

public class FileUploadInterceptor implements HandlerInterceptor {
    @Value("${uploadfiledir.maxFileSize}")
    private String maxFileSize;
 
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(request!=null && ServletFileUpload.isMultipartContent(request)) {
            ServletRequestContext ctx = new ServletRequestContext(request);
            long requestSize = ctx.contentLength();
            if (requestSize > StringUtil.sizeFormat(maxFileSize)) {
                throw new MyException("上传文件大小不能超过" + maxFileSize);
            }
        }
        return true;
    }
 
 
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
 
    }
 
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
 
}
