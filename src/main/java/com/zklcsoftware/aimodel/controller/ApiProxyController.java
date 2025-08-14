package com.zklcsoftware.aimodel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.zklcsoftware.common.web.ExtBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
@RequestMapping(path = {"", "/api"})
public class ApiProxyController extends ExtBaseController {
    @Autowired
    private OAuth2ClientContext oauth2Context;
    private final RestTemplate restTemplate;
    @Value("${sys.netname}")
    String targetUrl;//目标系统的URL

    public ApiProxyController(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @PostMapping("/proxy/**")
    public Object proxyRequest(HttpServletRequest request, HttpServletResponse response,
                                               @RequestBody(required = false) String body,
                               @RequestHeader Map<String, String> headers) {
        try {
            // 获取原始请求路径并去除前缀
            String originalPath = request.getRequestURI().split("/proxy/")[1];
            String fullTargetUrl = targetUrl + "/" + originalPath;
            HttpHeaders requestHeaders = new HttpHeaders();
            headers.forEach((key, value) -> {
                if (!"Transfer-Encoding".equalsIgnoreCase(key)) {
                    requestHeaders.set(key, value);
                }
            });
            requestHeaders.set("Authorization","Bearer "+oauth2Context.getAccessToken().getValue());//设置用户Token
            HttpEntity<String> requestEntity = new HttpEntity<>(body, requestHeaders);
            ResponseEntity<String> res = restTemplate.exchange(
                    fullTargetUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            HttpHeaders responseHeaders = new HttpHeaders();
            res.getHeaders().forEach((key, values) -> {
                if (!"Transfer-Encoding".equalsIgnoreCase(key)) {
                    responseHeaders.put(key, values);
                }
            });

            return ResponseEntity.status(res.getStatusCode())
                    .headers(responseHeaders)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(res.getBody());
        } catch (Exception e) {
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
