
import com.google.protobuf.ByteString;
import com.modules.vt_utils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;


public class test {
    private static final Logger logger= LoggerFactory.getLogger(test.class);
    @Test
    public void test(){
        // 常规http request 请求
        String url= "https://live.bilibili.com/24177801?session_id=a292b2fd1606993372e49104d6d878a1_607C485F-6C5D-4791-B596-D3BCE0DCEE0B&launch_id=1000000";
        HttpUriRequest request = RequestBuilder.get(url)
                .build();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            CloseableHttpResponse response = httpClient.execute(request);
            OutputStream is = new FileOutputStream("C:\\Users\\Administrator\\Desktop\\pr_plugin\\vt_api\\src\\test\\java\\test.log");
            response.getEntity().writeTo(is);
        } catch (IOException e) {
            logger.info("异常");
        }

    }

    @Test
    public void test1(){
        // selenium 测试
        System.setProperty("webdriver.chrome.driver","H:\\chromeDriver\\101\\chromedriver_win32\\chromedriver.exe");
        ChromeOptions chromeOptions =new ChromeOptions();
        chromeOptions.setHeadless(false);
        WebDriver driver = new ChromeDriver(chromeOptions);
        new WebDriverWait(driver, Duration.ofMillis(3000),Duration.ofMillis(500)).until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@id=\"chat-items\"]/div")));
        String room_id="115";
        driver.get("https://live.bilibili.com/blanc/"+room_id+"?liteVersion=true");
        int read_times=0;
        int index=0;
        while(true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            read_times++;
            // 将元素标记为当前批次
            ((ChromeDriver) driver).executeScript(
                    "var elements=document.getElementById(\"chat-items\").childNodes\n" +
                    "for (var i = 0; i < elements.length; i++) {\n" +
                    "    var element = elements[i];\n" +
                    "    if (element[\"read_times\"]==undefined){\n" +
                    "    element[\"read_times\"]= "+ read_times +"\n" +
                    "    }\n" +
                    "}\n");
            List<WebElement> chat_elements = driver.findElements(By.xpath("//div[@id=\"chat-items\"]/div"));
            int message_num=chat_elements.size();
            if (message_num==0){
                continue;
            }
            for (int i=0;i<message_num;i++){
                WebElement element = chat_elements.get(i);

                String tmp_read_times="";
                try{
                    tmp_read_times=element.getAttribute("read_times");
                }catch (Exception e){
                    continue;
                }
                if (!String.valueOf(read_times).equals(tmp_read_times)){
                    continue;
                }
                index++;

                if (element.getAttribute("data-uname") != null && element.getAttribute("data-danmaku") != null){
                    String user_name=element.getAttribute("data-uname");
                    String message=element.getAttribute("data-danmaku");
                    logger.info("消息"+index+"|"+user_name+":"+message);
                } else if (Arrays.asList(element.getAttribute("class").split(" ")).contains("gift-item")) {
                    String[] gift_info=element.getText().split("\n");
                    String user_name="";
                    String gift="";
                    String gift_num="1";
                    for (String line : gift_info){
                        if (line.contains("投喂")){
                            user_name=line.split("投喂")[0];
                            gift=line.split("投喂")[1];
                        }else if(line.contains("共") && line.contains("个")){
                            gift_num=line.replace("共","").replace("个","");
                        }else if(line.startsWith("x")){
                            gift_num=line.replace(" ","").replace("x","");
                        }else{
                            continue;
                        }
                    }

                    logger.info("礼物"+index+"|"+user_name+":"+gift+":"+gift_num);
                }else{
                    logger.info("无效"+index);
                }


            }
        }


    }

    @Test
    public void user_ico_get(){
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                SSLContexts.createDefault(),
                new String[] { "TLSv1.2" },
                null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier());

        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        String user_id="160032492";
        String user_name="虎虎ok";
        String url= "https://tenapi.cn/bilibili/?uid="+user_id;
        HttpUriRequest url_request = RequestBuilder.get(url)
                .addHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                .build();
        try {
            CloseableHttpResponse response = httpClient.execute(url_request);
            OutputStream os = new ByteArrayOutputStream();
            response.getEntity().writeTo(os);
            String tmp_img_url=new String(((ByteArrayOutputStream) os).toByteArray(),"utf8");
            String img_url = tmp_img_url.split("\"avatar\": \"")[1].split("\"")[0];
            HttpUriRequest img_request = RequestBuilder.get(img_url)
                    .addHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                    .build();
            CloseableHttpResponse img_response = httpClient.execute(img_request);
            OutputStream img_os = new FileOutputStream("C:\\Users\\Administrator\\Desktop\\pr_plugin\\vt_api\\src\\test\\java\\"+user_name+".jpg");
            img_response.getEntity().writeTo(img_os);
            logger.info("下载头像成功：用户名："+user_name);
        } catch (Exception e) {
            logger.info("下载头像异常，用户名："+user_name);
            e.printStackTrace();
        }
    }

    @Test
    public void test3(){
        String user_name="虎虎ok";
        String url= "https://search.bilibili.com/upuser?keyword="+user_name+"&from_source=webtop_search";
        System.setProperty("webdriver.chrome.driver","H:\\chromeDriver\\101\\chromedriver_win32\\chromedriver.exe");
        ChromeOptions chromeOptions =new ChromeOptions();
        chromeOptions.setHeadless(false);
        WebDriver driver = new ChromeDriver(chromeOptions);
        driver.get(url);
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                SSLContexts.createDefault(),
                new String[] { "TLSv1.2" },
                null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier());

        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        new WebDriverWait(driver, Duration.ofMillis(3000),Duration.ofMillis(500)).until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"i_cecream\"]//img[@class=\"bili-avatar-img bili-avatar-face bili-avatar-img-radius\"]")));
        String img_url=driver.findElement(By.xpath("//*[@id=\"i_cecream\"]//img[@class=\"bili-avatar-img bili-avatar-face bili-avatar-img-radius\"]")).getAttribute("data-src").split("@")[0];
        logger.info(img_url);
        try {
            HttpUriRequest img_request = RequestBuilder.get(img_url)
                    .addHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                    .build();
            CloseableHttpResponse img_response = httpClient.execute(img_request);
            OutputStream img_os = new FileOutputStream("C:\\Users\\Administrator\\Desktop\\pr_plugin\\vt_api\\src\\test\\java\\"+user_name+".jpg");
            img_response.getEntity().writeTo(img_os);
            logger.info("下载头像成功：用户名："+user_name);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("下载头像异常，用户名："+user_name);
            e.printStackTrace();
        }
    }

    @Test
    public void test4(){
//        logger.info("12345".substring(0,"12345".length()-1));
//        if(null == null)
//            logger.info("hi");
//        logger.info(new HashSet(new ArrayList()).toString());
//        try {
//            Runtime.getRuntime().exec("cmd /c mkdir C:\\Users\\Administrator\\Desktop\\tank\\Assets\\images\\usersIco\\test");
//        } catch (IOException e) {
//            e.printStackTrace();
//            if (!e.getMessage().contains("已经存在")){
//                logger.info("删除目录失败：");
//            }


        }
    @Test
    public void test5(){
//        System.out.println(vt_utils.exe_cmd("ffmpeg"));
        System.out.println(vt_utils.regex_match_group("tmpa=1.23332b=4",".+a=([0-9]+\\.[0-9]{3})",1));

    }
    }


