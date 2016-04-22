package utils.opglutil;

/**
 * Created by jay_zs on 15/12/22.
 */
public class ArkMath {

    public static float min(float... fs) {
        int length = fs.length;
        if (length == 0) {
            return 0;
        }

        float min = fs[0];
        for (int i = 1; i < length; ++i) {
            min = Math.min(fs[i], min);
        }

        return min;
    }

    public static float max(float... fs) {
        int length = fs.length;
        if (length == 0) {
            return 0;
        }

        float max = fs[0];
        for (int i = 1; i < length; ++i) {
            max = Math.max(fs[i], max);
        }

        return max;
    }
}
