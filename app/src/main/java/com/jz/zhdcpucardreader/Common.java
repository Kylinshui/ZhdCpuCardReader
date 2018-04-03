package com.jz.zhdcpucardreader;

/**
 * Created by root on 18-1-26.
 */

public class Common {

    private final static char[] HEX_DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    public static String BytestoHexString(byte[] array, int length) {
        return toHexString(array, 0, length);
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


    public static byte[] hexStringToByteArray(String hexString) {
        int length = hexString.length();
        byte[] buffer = new byte[length / 2];

        for (int i = 0; i < length; i += 2) {
            buffer[i / 2] = (byte) ((toByte(hexString.charAt(i)) << 4) | toByte(hexString
                    .charAt(i + 1)));
        }

        return buffer;
    }

    private static int toByte(char c) {
        if (c >= '0' && c <= '9')
            return (c - '0');
        if (c >= 'A' && c <= 'F')
            return (c - 'A' + 10);
        if (c >= 'a' && c <= 'f')
            return (c - 'a' + 10);

        throw new RuntimeException("Invalid hex char '" + c + "'");
    }


    public static boolean isHex(String str){
        if(str.length()%2!=0){
            return false;
        }
        boolean flag = true;
        for(int i=0; i<str.length();i++){
            int l = str.charAt(i);
            if ((((l < 48) || (l > 57))) && (((l < 65) || (l > 70))) && (((l < 97) || (l > 102))))
                flag = false;
        }

        return flag;
    }
}
