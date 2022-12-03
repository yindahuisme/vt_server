package com.modules;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.ssssssss.magicapi.core.annotation.MagicModule;
import org.ssssssss.script.annotation.Comment;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@MagicModule("vt_utils")
public class vt_utils {
    private static Runtime runtime = Runtime.getRuntime();
    @Comment("执行cmd,返回状态码，标准输出")
    public static Map exe_cmd(String cmdStr){
        try {
            Process process = runtime.exec(cmdStr);
            process.waitFor();
            String stateCode = String.valueOf(process.exitValue());
            InputStream suc_is = process.getInputStream();
            InputStream fail_is = process.getErrorStream();
            Map res = new HashMap<String,String>();
            res.put("stateCode",stateCode);
            res.put("sucMessage",is_to_str(suc_is,"gbk"));
            res.put("failMessage",is_to_str(fail_is,"gbk"));
            return res;

        } catch (Exception e) {
            throw new RuntimeException("cmd执行失败,命令："+ cmdStr + "，异常："+e.toString());
        }

    }

    @Comment("inputstream转换字符串")
    public static String is_to_str (InputStream is,String charset) throws IOException {
        StringBuilder content= new StringBuilder();
        InputStreamReader isr = new InputStreamReader(is,charset);
        BufferedReader br = new BufferedReader(isr);
        String tmpStr = br.readLine();
        while (tmpStr != null) {
            content.append(tmpStr);
            tmpStr = br.readLine();
        }
        return content.toString();
    }


    @Comment("正则匹配，返回某一个捕获组内容")
    public static String regex_match_group (String tarStr,String pattern,int index) {
        // 创建 Pattern 对象
        Pattern r = Pattern.compile(pattern);

        // 创建 matcher 对象
        Matcher m = r.matcher(tarStr);
        m.find();
        return m.group(index);
    }

}
