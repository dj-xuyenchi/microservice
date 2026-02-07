package com.erp.util;

public class StringValid {
    public static boolean isNullOrEmpty(final String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNullOrWhiteSpace(final String str) {
        return str == null || str.trim().isEmpty();
    }

    public static String unicode2Latin(final String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        final char[] chars = str.toCharArray();
        final StringBuilder result = new StringBuilder();

        for (int i = 0; i < chars.length; i++) {
            final char c = chars[i];
            switch (c) {
                case 'à':
                case 'á':
                case 'ạ':
                case 'ả':
                case 'ã':
                case 'â':
                case 'ầ':
                case 'ấ':
                case 'ậ':
                case 'ẩ':
                case 'ẫ':
                case 'ă':
                case 'ằ':
                case 'ắ':
                case 'ặ':
                case 'ẳ':
                case 'ẵ':
                    result.append('a');
                    break;
                case 'è':
                case 'é':
                case 'ẹ':
                case 'ẻ':
                case 'ẽ':
                case 'ê':
                case 'ề':
                case 'ế':
                case 'ệ':
                case 'ể':
                case 'ễ':
                    result.append('e');
                    break;
                case 'ì':
                case 'í':
                case 'ị':
                case 'ỉ':
                case 'ĩ':
                    result.append('i');
                    break;
                case 'ò':
                case 'ó':
                case 'ọ':
                case 'ỏ':
                case 'õ':
                case 'ô':
                case 'ồ':
                case 'ố':
                case 'ộ':
                case 'ổ':
                case 'ỗ':
                case 'ơ':
                case 'ờ':
                case 'ớ':
                case 'ợ':
                case 'ở':
                case 'ỡ':
                    result.append('o');
                    break;
                case 'ù':
                case 'ú':
                case 'ụ':
                case 'ủ':
                case 'ũ':
                case 'ư':
                case 'ừ':
                case 'ứ':
                case 'ự':
                case 'ử':
                case 'ữ':
                    result.append('u');
                    break;
                case 'ỳ':
                case 'ý':
                case 'ỵ':
                case 'ỷ':
                case 'ỹ':
                    result.append('y');
                    break;
                case 'đ':
                    result.append('d');
                    break;
                case 'À':
                case 'Á':
                case 'Ạ':
                case 'Ả':
                case 'Ã':
                case 'Â':
                case 'Ầ':
                case 'Ấ':
                case 'Ậ':
                case 'Ẩ':
                case 'Ẫ':
                case 'Ă':
                case 'Ằ':
                case 'Ắ':
                case 'Ặ':
                case 'Ẳ':
                case 'Ẵ':
                    result.append('A');
                    break;
                case 'È':
                case 'É':
                case 'Ẹ':
                case 'Ẻ':
                case 'Ẽ':
                case 'Ê':
                case 'Ề':
                case 'Ế':
                case 'Ệ':
                case 'Ể':
                case 'Ễ':
                    result.append('E');
                    break;
                case 'Ì':
                case 'Í':
                case 'Ị':
                case 'Ỉ':
                case 'Ĩ':
                    result.append('I');
                    break;
                case 'Ò':
                case 'Ó':
                case 'Ọ':
                case 'Ỏ':
                case 'Õ':
                case 'Ô':
                case 'Ồ':
                case 'Ố':
                case 'Ộ':
                case 'Ổ':
                case 'Ỗ':
                case 'Ơ':
                case 'Ờ':
                case 'Ớ':
                case 'Ợ':
                case 'Ở':
                case 'Ỡ':
                    result.append('O');
                    break;
                case 'Ù':
                case 'Ú':
                case 'Ụ':
                case 'Ủ':
                case 'Ũ':
                case 'Ư':
                case 'Ừ':
                case 'Ứ':
                case 'Ự':
                case 'Ử':
                case 'Ữ':
                    result.append('U');
                    break;
                case 'Ỳ':
                case 'Ý':
                case 'Ỵ':
                case 'Ỷ':
                case 'Ỹ':
                    result.append('Y');
                    break;
                case 'Đ':
                    result.append('D');
                    break;
                default:
                    result.append(c);
                    break;
            }
        }
        return result.toString();
    }

}

