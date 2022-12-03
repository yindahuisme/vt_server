package com.modules;
import ch.qos.logback.core.encoder.EchoEncoder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.ssssssss.script.annotation.Comment;
import org.springframework.stereotype.Component;
import org.ssssssss.magicapi.core.annotation.MagicModule;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Component
@MagicModule("bilibili_comment_gift_get")
@ConfigurationProperties(prefix="bilibili")
public class bilibili_comment_gift_get {
    private static final Logger logger= LoggerFactory.getLogger(bilibili_comment_gift_get.class);

    //读取页面次数
    private int flush_times=0;
    //当前读取的消息数
    private int index=0;
    //浏览器对象
    private WebDriver driver=null;
    //浏览器对象（玩家头像）
    private WebDriver ico_driver = null;
    //http请求客户端对象
    private CloseableHttpClient httpClient = null;
    //浏览器驱动位置
    private String driver_path;
    //直播房间id
    private String room_id;
    //玩家头像存放位置
    private String user_ico_path;
    //聊天元素定位
    private By chat_by=null;
    //玩家头像元素定位
    private By ico_by=null;

    @Comment("初始化")
    public void init() {
        logger.info("初始化bilibili评论，礼物爬虫开始");
        this.chat_by=By.xpath("//div[@id=\"chat-items\"]/div");
        this.ico_by=By.xpath("//div[@class=\"h-user\"]//img[@class=\"bili-avatar-img bili-avatar-face bili-avatar-img-radius\"]");
        System.setProperty("webdriver.chrome.driver",this.driver_path);
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                SSLContexts.createDefault(),
                new String[] { "TLSv1.2" },
                null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier());
        this.httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        ChromeOptions chromeOptions =new ChromeOptions();
        chromeOptions.addArguments("--disable-gpu --headless --disable-software-rasterizer blink-settings=imagesEnabled=false --disable-extensions");

        if (this.driver==null){
            this.driver = new ChromeDriver(chromeOptions);
        }
        if (this.ico_driver==null){
            this.ico_driver=new ChromeDriver(chromeOptions);
        }
        this.driver.get("https://live.bilibili.com/blanc/"+this.room_id+"?liteVersion=true");
        new WebDriverWait(this.driver, Duration.ofMillis(3000),Duration.ofMillis(500)).until(
                ExpectedConditions.presenceOfElementLocated(this.chat_by));
        logger.info("初始化结束");
    }

    @Comment("从当前页面获得评论，礼物信息")
    public HashMap flush() {
        this.flush_times++;
        logger.info("第" + this.flush_times + "次读取页面");
        //玩家消息分成三类：普通消息，礼物，阵营
        HashMap<String,ArrayList> result = new HashMap<String,ArrayList>();
        result.put("普通消息",new ArrayList<HashMap<String,String>>());
        result.put("礼物",new ArrayList<HashMap<String,String>>());
        result.put("阵营",new ArrayList<HashMap<String,String>>());
        // 将元素标记为当前批次
        ((ChromeDriver) driver).executeScript(
                "var elements=document.getElementById(\"chat-items\").childNodes\n" +
                        "for (var i = 0; i < elements.length; i++) {\n" +
                        "    var element = elements[i];\n" +
                        "    if (element[\"read_times\"]==undefined){\n" +
                        "        if (element[\"class\"]==\"chat-item gift-item\" && ! element.innerText().includes(\"投喂\")){\n" +
                        "        continue;" +
                        "        }\n" +
                        "        element[\"read_times\"]= " + this.flush_times + "\n" +
                        "    }\n" +
                        "}\n");
        List<WebElement> chat_elements = driver.findElements(this.chat_by);
        int message_num = chat_elements.size();
        if (message_num == 0) {
            return result;
        }
        for (int i = 0; i < message_num; i++) {
            WebElement element = chat_elements.get(i);

            String tmp_read_times = "";
            try {
                tmp_read_times = element.getAttribute("read_times");
            } catch (Exception e) {
                continue;
            }
            if (!String.valueOf(this.flush_times).equals(tmp_read_times)) {
                continue;
            }
            this.index++;
            String message_str="";
            if (element.getAttribute("data-uname") != null && element.getAttribute("data-danmaku") != null) {
                String user_name = element.getAttribute("data-uname");
                String message = element.getAttribute("data-danmaku");
                String user_id = element.getAttribute("data-uid");

                if(message.equals("魏")||message.equals("蜀")||message.equals("吴")||message.equals("群")){
                    HashMap tmp_message=new HashMap<String,String>();
                    tmp_message.put("user_name",user_name);
                    tmp_message.put("user_id",user_id);
                    tmp_message.put("message",message);
                    result.get("阵营").add(tmp_message);
                }else{
                    HashMap tmp_message=new HashMap<String,String>();
                    tmp_message.put("user_name",user_name);
                    tmp_message.put("message",message);
                    result.get("普通消息").add(tmp_message);
                }
                message_str="消息" + this.index + "|" + user_name + ":" + message;
                logger.info(message_str);
            } else if (Arrays.asList(element.getAttribute("class").split(" ")).contains("gift-item")) {
                String[] gift_info = element.getText().split("\n");
                String user_name = "";
                String gift = "";
                String gift_num = "0";
                //礼物状态：单送，多送，连击，总计
                String gift_state="";
                for (String line : gift_info) {
                    if (line.contains("投喂")) {
                        user_name = line.split("投喂")[0];
                        gift = line.split("投喂")[1];
                        gift_num="1";
                        gift_state="单送";
                    } else if (line.contains("共") && line.contains("个")) {
                        gift_num = String.valueOf(Integer.valueOf(line.replace("共", "").replace("个", ""))-1);
                        gift_state="总计";
                    } else if (line.startsWith("x")) {
                        gift_num = line.replace(" ", "").replace("x", "");
                        gift_state="多送";
                    } else if (line.endsWith("连击")) {
                        gift_num="0";
                        gift_state="连击";
                    }else {
                        continue;
                    }
                    logger.info("--:"+line);
                }
                if (gift_state.equals("单送")||gift_state.equals("多送")||gift_state.equals("总计")) {
                    HashMap tmp_message = new HashMap<String, String>();
                    tmp_message.put("user_name", user_name);
                    tmp_message.put("gift", gift);
                    tmp_message.put("gift_num", gift_num);
                    result.get("礼物").add(tmp_message);
                }
                message_str="礼物" + this.index + "|" + user_name + ":" + gift + ":" + gift_num;
                logger.info(message_str);
            } else {
                logger.info("无效" + this.index);
            }
        }
        logger.info("读取完成");
        return result;
    }

    @Comment("下载玩家头像")
    public boolean user_ico_get(String user_id,String user_name){
        String url= "https://space.bilibili.com/"+user_id;
        String img_local_path=this.user_ico_path;
        try {
            Runtime.getRuntime().exec("cmd /c mkdir "+img_local_path);
        } catch (IOException e) {
            if (!e.getMessage().contains("已经存在")){
                logger.info("创建目录失败："+img_local_path);
                return false;
            }
        }
        this.ico_driver.get(url);
        try {
            new WebDriverWait(this.ico_driver, Duration.ofMillis(5000), Duration.ofMillis(500)).until(
                    ExpectedConditions.presenceOfElementLocated(this.ico_by));
        }catch (Exception e){
            logger.info("下载头像超时，用户名："+user_name);
            return false;
        }
        String img_url=ico_driver.findElement(this.ico_by).getAttribute("data-src").split("@")[0];
        try {
            HttpUriRequest img_request = RequestBuilder.get(img_url)
                    .addHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                    .build();
            CloseableHttpResponse img_response = this.httpClient.execute(img_request);
            OutputStream img_os = new FileOutputStream(img_local_path+user_name+".jpg");
            img_response.getEntity().writeTo(img_os);
            img_os.close();
            logger.info("下载头像成功：用户名："+user_name);
            return true;
        } catch (Exception e) {
            logger.info("下载头像异常，用户名："+user_name);
            return false;
        }
    }

    public String getDriver_path() {
        return driver_path;
    }

    public void setDriver_path(String driver_path) {
        this.driver_path = driver_path;
    }

    public String getRoom_id() {
        return room_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
    }

    public String getUser_ico_path() {
        return user_ico_path;
    }

    public void setUser_ico_path(String user_ico_path) {
        this.user_ico_path = user_ico_path;
    }
}


