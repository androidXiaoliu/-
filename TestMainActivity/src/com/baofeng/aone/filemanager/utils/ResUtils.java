package com.baofeng.aone.filemanager.utils;

import java.util.HashMap;
import java.util.Map;

public class ResUtils {

    static Map<String, String> typeMap = new HashMap<>();
    public static boolean isLoadedTypeMap = false;

    public static void init() {
        addMiniTypeToMap();
    }

    public static final String filetype_sysfolder = "Sys Folder";
    public static final String picturefile = "Picture file";
    public static final String audiofile = "Audio file";
    public static final String compressfile = "Compressed file";
    public static final String htmlfile = "Html file";
    public static final String videofile = "Video file";
    public static final String unknownfile = "UnKnown file";
    public static final String word_file = "Word document";
    public static final String excel_file = "Excel document";
    public static final String ppt_file = "PowerPoint document";
    public static final String pdf_file = "PDF document";
    public static final String txt_file = "Text document";
    public static final String filetype_folder = "Folder";
    public static final String error_file_does_not_exists = "File does not exist.";
    public static final String Document = "Office document";
    public static final String android_file = "Android installation file";
    public static final String vcf_file = "vCard";
    public static final String csv_file = "csv file";
    public static final String fi_type_ebook = "Ebook";
    public static final String fi_type_cert = "Electronic Certificate";
    public static final String storage_prefix = "Removable disk";
    public static final String sdcard = "SD card";

    public static String[] fileEndingImage = { ".png", ".gif", ".jpg", ".jpeg",
            ".bmp", ".wbmp" };
    public static String[] fileEndingWebText = { ".htm", ".html", ".php",
            ".xml" };
    public static String[] fileEndingPackage = { ".jar", ".zip", ".jad", ".gz" };
    public static String[] fileEndingAudio = { ".mp3", ".wav", ".ogg", ".midi",
            ".mid", ".wma", ".amr", ".aac", ".m4a", ".ra", ".amr", ".au",
            ".aiff", ".ogm", ".flac", ".ape" };
    public static String[] fileEndingVideo = { ".mp4", ".rm", ".mpg", ".avi",
            ".mpeg", ".3gp", ".m4v", ".rmvb", ".wmv", ".flv", ".f4v", ".mov",
            ".rv", ".asf", ".mkv", ".ts" };
    public static String[] fileEndingOffice = { ".doc", ".docx", ".xls",
            ".xlsx", ".ppt", ".pptx" };
    public static String[] fileEndingAndroid = { ".apk", ".lca" };
    public static String[] fileEndingEbook = { ".pdf", ".epub" };
    public static String[] fileEndingCert = { ".p12", ".crt", ".cer" };

    private static String[] typeKey = { ".doc", ".html", ".asf", ".xls",
            ".csv", ".m4a", ".ra", ".wbmp", ".txt", ".au", ".lca", ".ogg",
            ".ogm", ".wma", ".rmvb", ".3gp", ".rv", ".fla", ".htm", ".apk",
            ".flac", ".ape", ".wmv", ".mkv", ".xml", ".rm", ".bmp", ".p12",
            ".png", ".gz", ".flv", ".amr", ".midi", ".mpeg", ".jpg", ".zip",
            ".mid", ".pptx", ".docx", ".aiff", ".cer", ".jad", ".wav", ".gif",
            ".jpeg", ".ics", ".mp3", ".mp4", ".f4a", ".epub", ".ts", ".jar",
            ".mpg", ".php", ".pdf", ".avi", ".aac", ".vcf", ".ppt", ".mov",
            ".xlsx", ".crt", ".m4v", ".f4v" };

    private static String[] typeValue = { "application/msword", "text/html",
            "video/asf", "application/msexcel", "text/csv", "audio/m4a",
            "audio/ra", "image/wbmp", "text/plain", "audio/au",
            "application/vnd.android.package-archive", "audio/ogg",
            "audio/ogm", "audio/wma", "video/rmvb", "video/3gp", "video/rv",
            "video/fla", "text/html",
            "application/vnd.android.package-archive", "audio/flac",
            "audio/x-ape", "video/wmv", "video/x-matroska", "text/xml",
            "video/rm", "image/bmp", "application/x-pkcs12", "image/png",
            "application/gzip", "video/flv", "audio/amr", "audio/midi",
            "video/mpeg", "image/jpg", "compressor/zip", "audio/mid",
            "application/mspowerpoint", "application/msword", "audio/aiff",
            "application/x-x509-ca-cert", "text/vnd.sun.j2me.app-descriptor",
            "audio/wav", "image/gif", "image/jpeg", "ics/calendar",
            "audio/mp3", "video/mp4", "audio/f4a", "application/epub+zip",
            "video/ts", "application/java-archive", "video/mpg", "text/php ",
            "application/pdf", "video/avi", "audio/aac", "contacts/vcf",
            "application/mspowerpoint", "video/mov", "application/msexcel",
            "application/x-x509-ca-cert", "video/m4v", "video/f4v" };

    private static void addMiniTypeToMap() {
        for (int i = 0; i < typeKey.length; i++) {
            typeMap.put(typeKey[i], typeValue[i]);
        }
    }

}
