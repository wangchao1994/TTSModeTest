package com.android.factory.led;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.content.Context;
import android.graphics.Color;
import com.android.factory.R;

public class LEDUtil {
    public static final int COLOR_OFF = -1;// 熄灭LED 灯
    public static final int COLOR_BREATHING = 0;// LED 呼吸灯
    public static final int COLOR_SPEAK_ME = 1;// 自己在讲话
    public static final int COLOR_SPEAK_OTHERS = 2;// 别人在讲话
    public static final int COLOR_LOGGING = 3;// 登录中...
    /**
     * 通知亮灯
     * ledID 编号
     * ledARGB 灯光颜色  A 即 Alpha（透明度） 0x00 透明 0xFF 为不透明
     * ledOnMS 灯亮的时间 单位毫秒 1000 , 2000 , ...
     * ledOffMS 灯灭的时间 单位毫秒 1000 , 2000 , ...
     * </p>
     */
    public static void noticeLED(Context context, int ledARGB, int ledOnMS, int ledOffMS) {
        final int ledID = 933062;
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(ledID);

		NotificationChannel channel = new NotificationChannel("foreground", "foregroundName", NotificationManager.IMPORTANCE_DEFAULT);
        nm.createNotificationChannel(channel);

		Notification notification = new Notification.Builder(context)
				.setSmallIcon(R.drawable.ic_handle)
				.setChannelId("foreground")
				.build();	

        if (ledARGB == COLOR_OFF) {
            // 熄灭LED 灯
            notification.ledARGB = Color.argb(0, 0, 0, 0);
            notification.ledOnMS = 0;
            notification.ledOffMS = 0;
            return;
        } else if (ledARGB == COLOR_BREATHING) {
            // 变为呼吸灯
            notification.ledARGB = Color.argb(255, 0, 0, 255);
            notification.ledOnMS = 500;
            notification.ledOffMS = 3000;
        } else if (ledARGB == COLOR_SPEAK_ME) {
            // 红
            notification.ledARGB = Color.argb(255, 255, 0, 0);
            notification.ledOnMS = ledOnMS;
            notification.ledOffMS = ledOffMS;
        } else if (ledARGB == COLOR_SPEAK_OTHERS) {
            // 绿
            notification.ledARGB = Color.argb(255, 0, 255, 0);
            notification.ledOnMS = ledOnMS;
            notification.ledOffMS = ledOffMS;
        } else if (ledARGB == COLOR_LOGGING) {
            //  登录中...
            notification.ledARGB = Color.argb(255, 0, 255, 0);
            notification.ledOnMS = 600;
            notification.ledOffMS = 600;
        } else if (ledARGB == Color.BLUE) {
            // 蓝
            notification.ledARGB = Color.argb(255, 0, 0, 255);
            notification.ledOnMS = ledOnMS;
            notification.ledOffMS = ledOffMS;
        }else {
            notification.ledARGB = ledARGB;
            notification.ledOnMS = ledOnMS;
            notification.ledOffMS = ledOffMS;
        }
        notification.flags = Notification.FLAG_SHOW_LIGHTS;
        notification.priority = Notification.PRIORITY_HIGH;
        nm.notify(ledID, notification);
    }

}
