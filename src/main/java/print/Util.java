package print;

import java.util.Date;

/**
 * Created by ricky on 02/11/2017.
 */
public class Util {
    public static String getCurrentTime() {
        return "[" + new Date().toString() + "] ";
    }
}
