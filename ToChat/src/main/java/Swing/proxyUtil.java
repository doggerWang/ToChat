package Swing;

public class proxyUtil {
    private static StringBuilder stringBuilder=new StringBuilder();

    public static String make(String type,String name,String time,String text ){
        stringBuilder.setLength(0);
        stringBuilder.append("GHW&").append(type).append("&").append(name).append("&").append(time).append("&").append(text);
        return stringBuilder.toString();
    }
    public static String make(String type,String name,String time ){
        stringBuilder.setLength(0);
        stringBuilder.append("GHW&").append(type).append("&").append(name).append("&").append(time).append("&").append(" ");
        return stringBuilder.toString();
    }
    public static String make(String type,int num ){
        stringBuilder.setLength(0);
        stringBuilder.append("GHW&").append(type).append("&").append(num);
        return stringBuilder.toString();
    }
}
