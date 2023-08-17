package love.forte.demo.service.impl;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import love.forte.demo.service.TranslateService;
import love.forte.demo.utils.translate.AuthV3Util;
import love.forte.demo.utils.translate.HttpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 网易有道智云翻译服务api调用demo
 * api接口: https://openapi.youdao.com/api
 */
@Service
public class TranslateServiceImpl implements TranslateService {

    @Value("${APP_KEY}")
    private String APP_KEY;
    @Value("${APP_SECRET}")
    private String APP_SECRET;
    public String getWord(String fanyi) {
        // 添加请求参数
        Map<String, String[]> params = createRequestParams(fanyi);
        // 添加鉴权相关参数
        try {
            AuthV3Util.addAuthParams(APP_KEY, APP_SECRET, params);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        // 请求api服务
        byte[] result = HttpUtil.doPost("https://openapi.youdao.com/api", null, params, "application/json");
        // 打印返回结果
        String chaxunResult = "";
        if (result != null) {
            String json = new String(result, StandardCharsets.UTF_8);
            JSONObject jsonObject = JSONUtil.parseObj(json);
            JSONObject basicObject = jsonObject.getJSONObject("basic");

            if(basicObject!=null){
                String explains = basicObject.getStr("explains");
               chaxunResult = explains.substring(1, explains.length()-1);
            }else {
                chaxunResult ="查询错误";
            }
        }
        return chaxunResult;
    }

    @Override
    public String fanyi(String fanyi) {
        // 添加请求参数
        Map<String, String[]> params = createRequestParams(fanyi);
        // 添加鉴权相关参数
        try {
            AuthV3Util.addAuthParams(APP_KEY, APP_SECRET, params);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        // 请求api服务
        byte[] result = HttpUtil.doPost("https://openapi.youdao.com/api", null, params, "application/json");
        // 打印返回结果
        String substring=null;
        if (result != null) {
            String json = new String(result, StandardCharsets.UTF_8);
            JSONObject jsonObject1 = JSONUtil.parseObj(json);
            String translation = jsonObject1.getStr("translation");
            substring = translation.substring(2, translation.length() - 2);

        }
        return substring;
    }


    private static Map<String, String[]> createRequestParams(String fanyi) {
        /*
         * note: 将下列变量替换为需要请求的参数
         * 取值参考文档: https://ai.youdao.com/DOCSIRMA/html/%E8%87%AA%E7%84%B6%E8%AF%AD%E8%A8%80%E7%BF%BB%E8%AF%91/API%E6%96%87%E6%A1%A3/%E6%96%87%E6%9C%AC%E7%BF%BB%E8%AF%91%E6%9C%8D%E5%8A%A1/%E6%96%87%E6%9C%AC%E7%BF%BB%E8%AF%91%E6%9C%8D%E5%8A%A1-API%E6%96%87%E6%A1%A3.html
         */
        String q = fanyi;
        String from = "auto";
        String to = "en";

        return new HashMap<String, String[]>() {{
            put("q", new String[]{q});
            put("from", new String[]{from});
            put("to", new String[]{to});
        }};
    }
}
