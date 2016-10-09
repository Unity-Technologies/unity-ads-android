package com.wds.ads.misc;

import android.content.Context;

import com.google.common.base.Charsets;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharSink;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.common.primitives.Bytes;
import com.scottyab.aescrypt.AESCrypt;
import com.wds.ads.R;
import com.wds.ads.log.DeviceLog;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.EncodedKeySpec;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DataUtil {
  private final Context context;
  private String destRoot;
  private String webRoot;
  private static final byte[] ivBytes = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
  private static final String AES_MODE = "AES/CBC/PKCS5Padding";
  private static final String HASH_ALGORITHM = "MD5";

  public DataUtil(Context context) {
    this.context = context;
    webRoot = context.getString(R.string.web_assets_root) + File.separator
      + context.getString(R.string.web_data_path) + File.separator;
    destRoot = context.getCacheDir() + File.separator + webRoot;
  }

  public void decryptData() {
    try {
      String source = webRoot + context.getString(R.string.data_zip_enc_path);
      InputStream input = context.getAssets()
        .open(source);

      File outputFile = new File(destRoot + File.separator +
        context.getString(R.string.data_zip_path));
      Files.createParentDirs(outputFile);

      String key = context.getString(R.string.key_name);
      SecretKeySpec secretKeySpec = generateKey(key);

      Cipher cipher = Cipher.getInstance(AES_MODE);
      IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

      cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);

      byte[] bytes = ByteStreams.toByteArray(input);
      byte[] decryptedBytes = cipher.doFinal(bytes);

      ByteSink output = Files.asByteSink(outputFile);
      output.write(decryptedBytes);

      DeviceLog.debug("data decrypted successfully");
    } catch (IOException | GeneralSecurityException e) {
      DeviceLog.exception("error decrypting data archive", e);
    }
  }


  private static SecretKeySpec generateKey(final String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
    final MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
    byte[] bytes = password.getBytes("UTF-8");
    digest.update(bytes, 0, bytes.length);
    byte[] key = digest.digest();
    return new SecretKeySpec(key, "AES");
  }

  public void extractData() {
    try {
      ByteSource byteSource = Files.asByteSource(
        new File(destRoot + context.getString(R.string.data_zip_path)));

      ZipInputStream zipInputStream = new ZipInputStream(byteSource.openBufferedStream());
      ZipEntry entry;

      while ((entry = zipInputStream.getNextEntry()) != null) {
        File file = new File(destRoot + entry.getName());

        if (file.exists()) {
          DeviceLog.debug(file.getAbsolutePath() + "\texists");
          continue;
        }

        if (entry.isDirectory()) {
          if (!file.exists()){
            boolean mkdirs = file.mkdirs();
          }
          DeviceLog.debug("mk directory: " + file.getAbsolutePath());
          continue;
        }

        DeviceLog.debug("extracting:" + entry + " to " + file.getAbsolutePath());

        ByteSink output = Files.asByteSink(file);
        output.writeFrom(zipInputStream);
      }
      zipInputStream.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
