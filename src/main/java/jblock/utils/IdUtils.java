package jblock.utils;

import java.util.UUID;
import com.gilt.timeuuid.TimeUuid;

public class IdUtils {

    public String getUUID(){
        return TimeUuid.apply().toString();
    }

    public String getRandomUUID(){
        return UUID.randomUUID().toString();
    }

    public static void main(String[] args){
        IdUtils idUtils = new IdUtils();
        System.out.println(idUtils.getUUID());
        System.out.println(idUtils.getUUID());
        System.out.println(idUtils.getUUID());
        System.out.println(idUtils.getUUID());
        System.out.println(idUtils.getUUID());
        System.out.println(idUtils.getRandomUUID());
    }
}
