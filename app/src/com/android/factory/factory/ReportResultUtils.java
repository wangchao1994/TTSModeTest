package com.android.factory.factory;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class ReportResultUtils {

    private static final String TAG = "ReportResultUtils";
    private static final String REPORT_TXT = "FactoryResult.txt";
    private static final String INTERNAL_DIR = "/storage/emulated/0/";

    public static void writeReportResult(String data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        byte[] buf = data.getBytes();
        try {
            File space_file = new File(INTERNAL_DIR,REPORT_TXT);
            if (!space_file.exists()) {
                space_file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(space_file, true);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fos);
            bufferedOutputStream.write(buf, 0, buf.length);
            bufferedOutputStream.write("\t,\t".getBytes());
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
            Log.e(TAG, "writeReportResult file------------>");
        } catch (Exception e) {
            Log.e(TAG, "Exception Trying to write file " + e.toString());
        }
        try {
            java.lang.Process p = Runtime.getRuntime().exec("chmod 777 -R " + "/storage/emulated/0/FactoryResult.txt");
            int status = p.waitFor();
            if (status == 0) {
                //chmod succeed
                Log.e(TAG, "writeResult filePath = " + INTERNAL_DIR + REPORT_TXT + " chmod succeed");
            } else {
                //chmod failed
                Log.e(TAG, "writeResult filePath = " + INTERNAL_DIR + REPORT_TXT + " chmod fail");
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception: Trying to write .REPORT_TXT file " + e.toString());
        }
    }
}
