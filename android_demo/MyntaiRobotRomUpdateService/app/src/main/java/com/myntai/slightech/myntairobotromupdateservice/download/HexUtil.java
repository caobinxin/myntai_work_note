package com.myntai.slightech.myntairobotromupdateservice.download;

public class HexUtil {
    private final static char[] HEX_DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    public static String getHexString(byte[] array) {
        return getHexString(array, 0, array.length);
    }

    public static String getHexString(byte[] array, int offset, int length) {
        StringBuilder result = new StringBuilder();

        for (int i = offset; i < offset + length; i++) {
            byte b = array[i];
            if (b > ' ' && b < '~') {
                result.append(new String(array, i, 1));
            } else {
                result.append(" ");
            }
        }

        return result.toString();
    }

    public static String toHexString(byte[] array) {
        return toHexString(array, 0, array.length);
    }

    public static String toHexString(byte[] array, int offset, int length) {
        char[] buf = new char[length * 2];

        int bufIndex = 0;
        for (int i = offset; i < offset + length; i++) {
            byte b = array[i];
            buf[bufIndex++] = HEX_DIGITS[(b >>> 4) & 0x0F];
            buf[bufIndex++] = HEX_DIGITS[b & 0x0F];
        }

        return new String(buf);
    }

    public static String toHexString(int i) {
        return toHexString(toByteArray(i));
    }

    public static byte[] toByteArray(int i) {
        byte[] array = new byte[4];

        array[3] = (byte) (i & 0xFF);
        array[2] = (byte) ((i >> 8) & 0xFF);
        array[1] = (byte) ((i >> 16) & 0xFF);
        array[0] = (byte) ((i >> 24) & 0xFF);

        return array;
    }
}
