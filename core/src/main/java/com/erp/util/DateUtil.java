package com.erp.util;


import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {
    public interface DateFormat {
        String TIME_ZONE = "Asia/Ho_Chi_Minh";
        String TYPE1 = "dd-MM-yyyy";
        String TYPE2 = "dd-MM-yyyy HH:mm:ss";
        String TYPE3 = "dd-MM-yyyy HH:mm";
        String TYPE4 = "MM-yyyy";
    }
    public static String dateToString(Date date, String pattern) {
        if (date != null && !DataUtil.isNullOrEmpty(pattern)) {
            java.text.DateFormat df = new SimpleDateFormat(pattern, new Locale("vi"));
            return df.format(date);
        } else {
            return null;
        }
    }

    public static Date safeToDate(Object obj1) {
        if (obj1 == null) {
            return null;
        } else {
            return obj1 instanceof Timestamp ? new Date(((Timestamp)obj1).getTime()) : (Date)obj1;
        }
    }
    public static boolean isBetween(Date d1, Date d2, Date input) {
        if (d1 == null || d2 == null || input == null) {
            return false; // hoặc throw IllegalArgumentException tuỳ yêu cầu
        }

        // Đảm bảo d1 là ngày nhỏ hơn
        Date start = d1.before(d2) ? d1 : d2;
        Date end = d1.after(d2) ? d1 : d2;

        return !input.before(start) && !input.after(end);
    }

    public static void main(String[] args) throws ParseException {
        Date from = new SimpleDateFormat("yyyy-MM-dd").parse("2024-06-01");
        Date to = new SimpleDateFormat("yyyy-MM-dd").parse("2024-06-30");
        Date check = new SimpleDateFormat("yyyy-MM-dd").parse("2024-05-1");

        System.out.println(isBetween(from, to, check)); // true
    }
}
