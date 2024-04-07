package com.example.ed_dicom_viewer;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** EdDicomViewerPlugin */
public class EdDicomViewerPlugin implements FlutterPlugin, MethodCallHandler {
    private MethodChannel channel;
    private EdDicomManager dicomManager;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "ed_dicom_viewer");
        channel.setMethodCallHandler(this);
        System.loadLibrary("imebra_lib");
        dicomManager = new EdDicomManager();
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals("getViewDicom")) {
            File file = new File((String) Objects.requireNonNull(call.argument("filePath")));
            if (!isDICOM(file)) {
                result.error("INVALID_FILE", "Invalid DICOM file", null);
                return;
            }
            ResponseModel data = null;
            try {
                data = dicomManager.readDicomFile(read(file));
                if (data != null) {
                    result.success(JsonUtil.convertToJson(data));
                }
            } catch (IOException e) {
                e.printStackTrace();
                result.error("INVALID_FILE", "Invalid DICOM file", null);
            }

        } else {
            result.notImplemented();
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }


    private byte[] read(File file) throws IOException {
        ByteArrayOutputStream ous = null;
        byte[] buffer = new byte[4096];
        ous = new ByteArrayOutputStream();
        try (InputStream ios = new FileInputStream(file)) {
            int read = 0;
            while ((read = ios.read(buffer)) != -1) {
                ous.write(buffer, 0, read);
            }
            ous.close();
        }
        return ous.toByteArray();
    }

    private boolean isDICOM(File file) {
        if (file.length() >= 132) {
            try (InputStream in = new FileInputStream(file)) {
                byte[] b = new byte[128 + 4];
                in.read(b, 0, 132);
                for (int i = 0; i < 128; i++)
                    if (b[i] != 0)
                        return false;
                if (b[128] != 68 || b[129] != 73 || b[130] != 67
                        || b[131] != 77)
                    return false;
            } catch (IOException e) {
                return false;
            }
            return true;
        }
        return false;
    }
}
