package jblock.utils;

public class TimeUtils {
    public static Long getCurrentTime(){
        Long time = System.currentTimeMillis();
        //中国时区+8
        time += 8*3600*1000;
        return time;
    }

    public static Long getNextTargetTimeDur(Long targetTime){
        System.out.println("Time is : " + targetTime);
        Long time = getCurrentTime();
        Long result = targetTime - time%targetTime;
        System.out.println("Time is : " + result);
        return result;
    }

    public static Long getNextTargetTimeDurMore(Long targetTime){
        Long time = getCurrentTime();
        Long result = targetTime - time%targetTime;
        System.out.println("Time is : " + (result+targetTime));
        return result+targetTime;
    }
}
