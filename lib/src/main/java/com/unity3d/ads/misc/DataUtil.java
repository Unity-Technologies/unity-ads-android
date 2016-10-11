package com.unity3d.ads.misc;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64InputStream;

import com.google.common.io.ByteSink;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.primitives.Bytes;
import com.google.gson.Gson;
import com.unity3d.ads.BuildConfig;
import com.unity3d.ads.R;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.models.Secure;
import com.unity3d.ads.properties.SdkProperties;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DataUtil {
  private static final String AES_MODE = "AES/CBC/NoPadding";

  private final Context context;
  private String destRoot;
  private String webRoot;

  public DataUtil(Context context) {
    this.context = context;
    webRoot = context.getString(R.string.web_assets_root) + File.separator
      + context.getString(R.string.web_data_path) + File.separator;
    destRoot = context.getFilesDir() + File.separator + webRoot;
  }

  private static byte[] hexStringToByteArray(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
        + Character.digit(s.charAt(i + 1), 16));
    }
    return data;
  }

  public boolean isInitialized() {
    File web = new File(destRoot);
    if (BuildConfig.DEBUG || !web.exists()) {
      return false;
    }

    SharedPreferences defaultSharedPreferences =
      PreferenceManager.getDefaultSharedPreferences(context);
    String versionCodeKey = context.getString(R.string.version_code_key);
    int versionCode = defaultSharedPreferences.getInt(versionCodeKey, 0);

    defaultSharedPreferences.edit()
      .putInt(versionCodeKey, SdkProperties.getVersionCode())
      .apply();

    return versionCode == SdkProperties.getVersionCode();
  }

  public void decryptData() {
    try {
      String source = webRoot + context.getString(R.string.data_zip_enc_path);
      InputStream input = context.getAssets()
        .open(source);

      File outputFile = new File(destRoot + File.separator +
        context.getString(R.string.data_zip_path));
      Files.createParentDirs(outputFile);

      InputStream secureInput = context.getAssets()
        .open(webRoot + context.getString(R.string.secure_json_path));

      String secureJson = new String(ByteStreams.toByteArray(secureInput));
      Secure secure = new Gson().fromJson(secureJson, Secure.class);

      byte[] keyBytes = hexStringToByteArray(secure.getKeyHex());

      Cipher cipher = Cipher.getInstance(AES_MODE);
      IvParameterSpec ivSpec = new IvParameterSpec(new byte[16]);

      cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), ivSpec);
      CipherInputStream cipherInputStream = new CipherInputStream(input, cipher);
      Base64InputStream base64InputStream = new Base64InputStream(cipherInputStream, 0);

      byte[] header = new byte[]{0x50, 0x4b, 0x03, 0x04, 0x14, 0x00,
        0x08, 0x00, 0x08, 0x00, 0x6b, 0x42};
      long skip = base64InputStream.skip(48);

      ByteSink byteSink = Files.asByteSink(outputFile);
      byteSink.write(Bytes.concat(header, ByteStreams.toByteArray(base64InputStream)));

      // byteSink.write(ByteStreams.toByteArray(cipherInputStream));
      base64InputStream.close();
      cipherInputStream.close();

      DeviceLog.debug("data decrypted successfully");
    } catch (IOException | GeneralSecurityException e) {
      DeviceLog.exception("error decrypting data archive", e);
    }
  }

  public void extractData() {
    try {
      InputStream open = context.getAssets()
        .open(webRoot + context.getString(R.string.data_zip_path));

      ZipInputStream zipInputStream = new ZipInputStream(open);
      ZipEntry entry;

      while ((entry = zipInputStream.getNextEntry()) != null) {
        File file = new File(destRoot + entry.getName());
        if (entry.isDirectory()) {
          continue;
        }

        if (file.exists()) {
          file.delete();
        }

        Files.createParentDirs(file);
        ByteSink output = Files.asByteSink(file);
        output.writeFrom(zipInputStream);
      }
      zipInputStream.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
