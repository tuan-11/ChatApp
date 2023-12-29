package com.zileanstdio.chatapp.Utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CipherUtils {
    public static class RSA {

        private static final String TAG = RSA.class.getSimpleName();

        public static byte[] decrypt(byte[] data, Key key) {
            try {
                javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("RSA");
                cipher.init(javax.crypto.Cipher.DECRYPT_MODE, key);
                return cipher.doFinal(data);
            } catch (Exception e)
            {
                Log.d(TAG, e.getMessage());
                return null;
            }

        }

        public static byte[] encrypt(byte[] original, Key key) {
            try {
                javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("RSA");
                cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key);
                return cipher.doFinal(original);
            } catch (Exception e)
            {
                Log.d(TAG, e.getMessage());
                return null;
            }

        }
    }

    public static class Shift {
        private static final String TAG = Shift.class.getSimpleName();

        public static boolean encryptFile(Context context, Uri uri, File encryptFile, byte[] key) {
            byte[] buffer = new byte[2048];
            int length;
            try {
                BufferedInputStream bufferedInputStream = new BufferedInputStream(context.getContentResolver().openInputStream(uri));

                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(encryptFile));
                while((length = bufferedInputStream.read(buffer)) != -1) {
                    buffer = encrypt(buffer, key);
                    bufferedOutputStream.write(buffer, 0, length);
                }
                bufferedOutputStream.close();
                bufferedInputStream.close();
                return true;
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
                return false;
            }
        }

        public static boolean encryptFile(File fileOriginal, File encryptFile, byte[] key) {
            byte[] buffer = new byte[2048];
            int length;
            try {
                BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(fileOriginal));
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(encryptFile));
                while((length = bufferedInputStream.read(buffer)) != -1) {
                    buffer = encrypt(buffer, key);
                    bufferedOutputStream.write(buffer, 0, length);
                }
                bufferedOutputStream.close();
                bufferedInputStream.close();
                return true;
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
                return false;
            }
        }

        public static boolean decryptFile(File encryptFile, File decryptFile, byte[] key) {
            byte[] buffer = new byte[2048];
            int length;
            try {
                BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(encryptFile));
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(decryptFile, true));
                while((length = bufferedInputStream.read(buffer)) != -1) {
                    buffer = decrypt(buffer, key);
                    bufferedOutputStream.write(buffer, 0, length);
                }
                bufferedOutputStream.close();
                bufferedInputStream.close();
                return true;
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
                return false;
            }
        }

        public static byte[] encrypt(byte[] buffer, byte[] key) {
            try {
                byte[] cipherBytes = new byte[buffer.length];

                for(int i = 0; i < buffer.length; i++) {
                    cipherBytes[i] = leftShift(buffer[i], key[i % key.length]);
                }
                return cipherBytes;

            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
                return null;
            }

        }

        public static byte[] decrypt(byte[] encrypted, byte[] key) {
            try {
                byte[] original = new byte[encrypted.length];

                for(int i = 0; i < encrypted.length; i++) {
                    original[i] = rightShift(encrypted[i], key[i % key.length]);
                }
                return original;
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
                return null;
            }
        }

        private static byte rightShift(byte b, int n) {
            n = (n > 8) ? n % 8 : n;
            int mask = b < 0 ? b ^ 0xffffff00 : b;
            int temp = mask << (8 - n);
            return (byte) (mask >> n | temp);
        }

        private static byte leftShift(byte b, int n) {
            n = (n > 8) ? 8 - (n % 8) : 8 -n;
            return rightShift(b, n);
        }

    }

    public static class Xor {
        private static final String TAG = Xor.class.getSimpleName();

        public static byte[] encrypt(byte[] buffer, byte[] key) {
            try {
                byte[] cipherBytes = new byte[buffer.length];

                for(int i = 0; i < buffer.length; i++) {
                    cipherBytes[i] = (byte) (buffer[i] ^ key[i % key.length]);
                }
                return cipherBytes;
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
                return null;
            }

        }

        public static byte[] decrypt(byte[] buffer, byte[] key) {
            try {
                byte[] originalBytes = new byte[buffer.length];

                for(int i = 0; i < buffer.length; i++) {
                    originalBytes[i] = (byte) (buffer[i] ^ key[i % key.length]);
                }
                return originalBytes;
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
                return null;
            }
        }
    }

    public static class AES {
        private static final String TAG = AES.class.getSimpleName();

        public static final String ALGORITHM = "AES/CBC/PKCS7Padding";

        public static SecretKey generateSecretKey(byte[] key) {
            key = Arrays.copyOf(key, 16);
            return new SecretKeySpec(key, "AES");
        }


        public static IvParameterSpec geneIvParameterSpec() {
            byte[] iv = new byte[16];
            Arrays.fill(iv, (byte) 0);
            return new IvParameterSpec(iv);
        }

        public static byte[] encrypt(byte[] original, byte[] key) {
            try {
                javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(ALGORITHM);
                cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, generateSecretKey(key), geneIvParameterSpec());
                return cipher.doFinal(original);

            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
                return null;
            }
        }

        public static byte[] decrypt(byte[] encrypted, byte[] key) {

            try {
                javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(ALGORITHM);
                cipher.init(javax.crypto.Cipher.DECRYPT_MODE, generateSecretKey(key), geneIvParameterSpec());

                return cipher.doFinal(encrypted);

            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
                return null;
            }

        }
    }

    public static class Hash {
        private static final String TAG = Hash.class.getSimpleName();

        public static String md5(File file) {
            return hash(file, "MD5");
        }

        public static String sha1(File file) {
            return hash(file, "SHA1");
        }

        public static String sha256(String input) { return hash(input, "SHA-256"); }

        private static String hash(String input, String type) {
            try {
                MessageDigest messageDigest = MessageDigest.getInstance(type);
                byte[] bytes = messageDigest.digest(input.getBytes(StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                for (byte aByte : bytes) {

                    sb.append(Character.forDigit((aByte >> 4) & 0xF, 16));
                    sb.append(Character.forDigit((aByte & 0xF), 16));
                }
                return sb.toString();
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
                return null;
            }
        }

        public static String hash(File file, String type) {
            byte[] buffer = new byte[2048];

            try(
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file))
            ) {
                MessageDigest messageDigest = MessageDigest.getInstance(type);
                int read;
                while((read = bufferedInputStream.read(buffer)) != -1) {
                    messageDigest.update(buffer, 0, read);
                }
                byte[] bytes = messageDigest.digest();
                StringBuilder sb = new StringBuilder();
                for (byte aByte : bytes) {

                    sb.append(Character.forDigit((aByte >> 4) & 0xF, 16));
                    sb.append(Character.forDigit((aByte & 0xF), 16));
                }
                return sb.toString();
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
                return null;
            }
        }
    }
}
