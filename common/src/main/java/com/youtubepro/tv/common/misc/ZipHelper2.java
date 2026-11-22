package com.youtubepro.tv.common.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Low-level zip helpers.
 *
 * NOTE (audit F-01): these methods never swallow exceptions anymore.
 * A failed zip/unzip must be visible to the caller, otherwise restore/export
 * flows may report success while data was lost or incomplete.
 */
public class ZipHelper2 {
    public static void zipDirectory(File sourceDir, File zipFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            zipFileRecursive(zos, sourceDir, sourceDir.getName() + "/");
        }
    }

    private static void zipFileRecursive(ZipOutputStream zos, File file, String base) throws IOException {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    zipFileRecursive(zos, child, base + child.getName() + "/");
                }
            }
        } else {
            try (FileInputStream fis = new FileInputStream(file)) {
                zos.putNextEntry(new ZipEntry(base.substring(0, base.length() - 1))); // strip "/" at the end to mark as file
                byte[] buf = new byte[8192];
                int len;
                while ((len = fis.read(buf)) > 0) zos.write(buf, 0, len);
                zos.closeEntry();
            }
        }
    }

    public static void unzip(File zipFile, File targetRoot) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            String canonicalRoot = targetRoot.getCanonicalPath() + File.separator;
            ZipEntry entry;
            byte[] buffer = new byte[8192];

            while ((entry = zis.getNextEntry()) != null) {
                File out = new File(targetRoot, entry.getName());

                String canonicalOut = out.getCanonicalPath();

                // Zip Slip protection
                if (!canonicalOut.startsWith(canonicalRoot)) {
                    throw new IOException("Blocked Zip Slip entry: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    out.mkdirs();
                } else {
                    out.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(out)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) fos.write(buffer, 0, len);
                    }
                }
                zis.closeEntry();
            }
        }
    }

    public static boolean hasRootDir(File zipFile, String rootDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;

            String prefix = rootDir + "/";

            while ((entry = zis.getNextEntry()) != null) {
                boolean match = entry.getName().startsWith(prefix);
                zis.closeEntry();
                if (match) {
                    return true;
                }
            }
        }

        return false;
    }
}
