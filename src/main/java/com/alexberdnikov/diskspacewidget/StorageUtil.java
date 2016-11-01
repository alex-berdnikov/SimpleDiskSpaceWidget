package com.alexberdnikov.diskspacewidget;


import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;


public class StorageUtil {
    private static final int BYTES_IN_MEGABYTE = 1048576;
    private static final Pattern DIR_SEPARATOR = Pattern.compile("/");

    /**
     * Raturns all available SD-Cards in the system (include emulated)
     *
     * Warning: Hack! Based on Android source code of version 4.3 (API 18)
     * Because there is no standart way to get it.
     * TODO: Test on future Android versions 4.4+
     *
     * @return paths to all available SD-Cards in the system (include emulated)
     */
    public static String[] getStorageDirectories() {
        // Final set of paths
        final Set<String> rv = new HashSet<>();

        // Primary physical SD-CARD (not emulated)
        final String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");

        // All Secondary SD-CARDs (all exclude primary) separated by ":"
        final String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");

        // Primary emulated SD-CARD
        final String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");

        if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
            // Device has physical external storage; use plain paths.
            if(TextUtils.isEmpty(rawExternalStorage)) {
                // EXTERNAL_STORAGE undefined; falling back to default.
                rv.add("/storage/sdcard0");
            } else {
                rv.add(rawExternalStorage);
            }
        } else {
            // Device has emulated storage; external storage paths should have
            // userId burned into them.
            final String rawUserId;

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                rawUserId = "";
            } else {
                final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                final String[] folders = DIR_SEPARATOR.split(path);
                final String lastFolder = folders[folders.length - 1];
                boolean isDigit = false;

                try {
                    Integer.valueOf(lastFolder);
                    isDigit = true;
                } catch(NumberFormatException ignored) {}

                rawUserId = isDigit ? lastFolder : "";
            }

            // /storage/emulated/0[1,2,...]
            if (TextUtils.isEmpty(rawUserId)) {
                rv.add(rawEmulatedStorageTarget);
            } else {
                rv.add(rawEmulatedStorageTarget + File.separator + rawUserId);
            }
        }

        // Add all secondary storages
        if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
            // All Secondary SD-CARDs splited into array
            final String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
            Collections.addAll(rv, rawSecondaryStorages);
        }

        return rv.toArray(new String[rv.size()]);
    }

    public static long getTotalBytes(String storagePath) {
        StatFs stat = new StatFs(storagePath);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return stat.getBlockCountLong() * stat.getBlockSizeLong();
        } else {
            return stat.getTotalBytes();
        }
    }

    public static long getTotalMegabytes(String storagePath) {
        return getTotalBytes(storagePath) / BYTES_IN_MEGABYTE;
    }

    public static long getAvailableBytes(String storagePath) {
        StatFs stat = new StatFs(storagePath);
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2
                ? (long) stat.getFreeBlocks() * (long) stat.getBlockSize()
                : stat.getFreeBlocksLong() * stat.getBlockSizeLong();
    }

    public static long getAvailableMegabytes(String storagePath) {
        return getAvailableBytes(storagePath) / BYTES_IN_MEGABYTE;
    }
}
