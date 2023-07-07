package com.lgh.advertising.going;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {

        //1.创建参数配置, 用于连接邮件服务器的参数配置
        Properties props = new Properties();                    //参数配置
        props.setProperty("mail.transport.protocol", "smtp");   //使用的协议（JavaMail规范要求）
        props.setProperty("mail.smtp.host", "smtp.qq.com");   //发件人的邮箱的 SMTP 服务器地址
        props.setProperty("mail.smtp.auth", "true");            //需要请求认证
        props.setProperty("mail.smtp.port", "587");
        props.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");

        props.put("mail.smtp.ssl.enable", "true");
//        props.put("mail.debug", "true");
        Session session = Session.getDefaultInstance(props);

        //3.创建一封邮件
        MimeMessage message = createMimeMessage(session, "2281442260@qq.com", "2893282695@qq.com");

        //4.根据 Session获取邮件传输对象
        Transport transport = session.getTransport();

        // 5.使用邮箱账号和密码连接邮件服务器, 这里认证的邮箱必须与 message中的发件人邮箱一致,否则报错
        //
        //    PS_01: 成败的判断关键在此一句, 如果连接服务器失败, 都会在控制台输出相应失败原因的 log,
        //           仔细查看失败原因, 有些邮箱服务器会返回错误码或查看错误类型的链接, 根据给出的错误
        //           类型到对应邮件服务器的帮助网站上查看具体失败原因。
        //
        //    PS_02: 连接失败的原因通常为以下几点, 仔细检查代码:
        //           (1)邮箱没有开启 SMTP 服务;
        //           (2)邮箱密码错误, 例如某些邮箱开启了独立密码;
        //           (3)邮箱服务器要求必须要使用 SSL 安全连接;
        //           (4)请求过于频繁或其他原因, 被邮件服务器拒绝服务;
        //           (5)如果以上几点都确定无误, 到邮件服务器网站查找帮助。
        //
        //PS_03:仔细看log,认真看log,看懂log,错误原因都在log已说明。
        transport.connect("2281442260@qq.com", "fbivwflrapkidjdf");
        //6.发送邮件,发到所有的收件地址,message.getAllRecipients()获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
        transport.sendMessage(message, message.getAllRecipients());
        System.out.println("邮件发送成功");
        // 7. 关闭连接

    }


    @Test
    public void test() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String str = simpleDateFormat.format(new Date());
        Random random = new Random();
        for (int n = 0; n < 200; n++) {
            System.out.println(Integer.parseInt((3 + random.nextInt(3)) + "" + random.nextInt(10)) - 8);
        }
    }


    public static MimeMessage createMimeMessage(Session session, String sendMail, String receiveMail) throws Exception {
        // 1.创建一封邮件
        MimeMessage message = new MimeMessage(session);
        // 2.From:发件人（昵称有广告嫌疑，避免被邮件服务器误认为是滥发广告以至返回失败，请修改昵称）
        message.setFrom(new InternetAddress(sendMail));
        // 3.To:收件人（可以增加多个收件人、抄送、密送）
        message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(receiveMail));
        // 4.Subject: 邮件主题（标题有广告嫌疑，避免被邮件服务器误认为是滥发广告以至返回失败，请修改标题）
        message.setSubject("打卡", "UTF-8");
        // 5.Content: 邮件正文（可以使用html标签）（内容有广告嫌疑，避免被邮件服务器误认为是滥发广告以至返回失败，请修改发送内容）
        message.setContent("成功", "text/html;charset=UTF-8");
        // 6.设置发件时间
        message.setSentDate(new Date());

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setDataHandler(new DataHandler(new FileDataSource("D:\\MyAndroidStudioProject\\ADGO2\\ADGO\\app\\src\\main\\res\\drawable\\support_me.png")));
        mimeBodyPart.setContentID("me.png");

        MimeBodyPart text = new MimeBodyPart();
        text.setContent("<br/><img src='cid:me.png'/><br/>", "text/html;charset=UTF-8");

        MimeMultipart mimeMultipart = new MimeMultipart();
        mimeMultipart.addBodyPart(mimeBodyPart);
        mimeMultipart.addBodyPart(text);
        mimeMultipart.setSubType("related");

        MimeBodyPart mimeBodyPart1 = new MimeBodyPart();
        mimeBodyPart1.setContent(mimeMultipart);

        MimeMultipart mimeMultipart1 = new MimeMultipart();
        mimeMultipart1.addBodyPart(mimeBodyPart1);
        mimeMultipart1.setSubType("mixed");

        message.setContent(mimeMultipart1);
        // 7.保存设置
        message.saveChanges();
        return message;
    }
}