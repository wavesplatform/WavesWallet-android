package com.wavesplatform.wallet.v2.ui.custom;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.Nullable;

import java.security.MessageDigest;
import java.util.Random;

public class Identicon {

    public static Bitmap create(String seed) {
        return create(seed, Options.DEFAULT);
    }

    public static Bitmap create(String seed, Options config) {
        return createBitmap(seed, config.rows, config.size, config.blankColor);
    }

    private static Bitmap createBitmap(String seed, int rows, int size, int blankColor) {
        if (size % rows != 0) {
            size = size + (rows - (size % rows));
        }
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
        int block = size / rows;
        boolean[][] mapping = mapToBit(seed, rows);
        if (mapping == null) {
            return bitmap;
        }
        int tintColor = getColor(seed);
        int[] pixels = new int[block * block];
        int[] blankPixels = new int[block * block];

        for (int p = 0; p < block * block; p++) {
            pixels[p] = tintColor;
            blankPixels[p] = blankColor;
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < rows; j++) {
                if (mapping[i][j]) {
                    bitmap.setPixels(pixels, 0, block, j * block, i * block, block, block);
                } else {
                    bitmap.setPixels(blankPixels, 0, block, j * block, i * block, block, block);
                }
            }
        }
        return bitmap;
    }

    public static boolean[][] mapToBit(String seed, int rows) {
        checkRows(rows);
        boolean[][] mapping = new boolean[rows][rows];
        char[] bits = getHash(seed);
        if (bits == null) {
            return null;
        }
        int stride = getStride(rows);
        int index = stride;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < (rows + 1) / 2; j++) {
                char c = bits[index];
                boolean b = c == '1';
                mapping[i][j] = b;
                mapping[i][rows - j - 1] = b;
                index += stride;
            }
        }
        return mapping;
    }

    private static void checkRows(int rows) {
        if ((rows & 0x1) == 0) {
            throw new IllegalArgumentException("Argument 'rows' must be an odd number");
        } else if (rows < 5 || rows > 11) {
            throw new IllegalArgumentException("Argument 'rows' must be between 5 and 11");
        }
    }

    private static
    @Nullable
    char[] getHash(String src) {
        String md5 = getBinMd5(src);
        if (md5 == null) {
            return null;
        } else {
            return md5.toCharArray();
        }
    }

    private static String getBinMd5(String src) {
        byte[] encryption = md5(src);
        if (encryption == null) {
            return null;
        }
        StringBuilder buffer = new StringBuilder();
        for (byte b : encryption) {
            buffer.append(Integer.toBinaryString((b & 0xFF) + 0x100).substring(1));
        }
        return buffer.toString();
    }

    private static int getStride(int rows) {
        return 128 / (rows * ((rows + 1) / 2));
    }

    private static byte[] md5(String src) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(src.getBytes("UTF-8"));
            return md5.digest();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getHexMd5(String src) {
        byte[] encryption = md5(src);
        if (encryption == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < encryption.length; i++) {
            if (Integer.toHexString(0xFF & encryption[i]).length() == 1) {
                sb.append("0").append(Integer.toHexString(0xFF & encryption[i]));
            } else {
                sb.append(Integer.toHexString(0xFF & encryption[i]));
            }
        }
        return sb.toString();
    }

    private static int getColor(String src) {
        String md5 = getHexMd5(src);
        if (md5 != null) {
            String rgb = md5.substring(md5.length() - 6, md5.length());
            return Color.parseColor("#" + rgb.toUpperCase());
        }

        return Color.BLACK;
    }

    public static class Options {

        public static final Options DEFAULT = new Options(Color.LTGRAY, 5, 100);

        int blankColor;
        int rows;
        int size;

        private Options(int blankColor, int rows, int size) {
            this.blankColor = blankColor;
            this.rows = rows;
            this.size = size;
        }

        public static class Builder {
            int bColor = Color.LTGRAY;
            int bRows = 5;
            int bSize = 100;

            public Builder setBlankColor(int color) {
                bColor = color;
                return this;
            }

            public Builder setRandomBlankColor() {
                Random rnd = new Random();
                bColor = Color.argb(255,
                        rnd.nextInt(256),
                        rnd.nextInt(256),
                        rnd.nextInt(256));
                return this;
            }

            public Builder setRows(int rows) {
                checkRows(rows);
                bRows = rows;
                return this;
            }

            public Builder setSize(int size) {
                bSize = size;
                return this;
            }

            public Options create() {
                return new Options(bColor, bRows, bSize);
            }

            private void checkRows(int rows) {
                if ((rows & 0x1) == 0) {
                    throw new IllegalArgumentException("Argument 'rows' must be an odd number");
                } else if (rows < 5 || rows > 11) {
                    throw new IllegalArgumentException("Argument 'rows' must be between 5 and 11");
                }
            }
        }
    }
}