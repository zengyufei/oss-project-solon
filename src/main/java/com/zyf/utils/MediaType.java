package com.zyf.utils;

import java.util.*;

/**
 *
 * @author Miroslav Nachev [mnachev@gmail.com]
 */
public enum MediaType {

    Application("application"),
    Audio("audio"),
    Example("example"),
    Chemical("chemical"),
    Image("image"),
    Message("message"),
    Model("model"),
    Multipart("multipart"),
    Text("text"),
    Video("video"),
    XConference("x-conference");

    private static Map<String, MediaType> mediaTypeMap;

    private final String mimeType;
    private final Set<String> mimeSubtypeSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    private MediaType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

    boolean addSubtype(String mimeSubtype) {
        return mimeSubtypeSet.add(mimeSubtype);
    }

    public Set<String> getMimeSubtypeSet() {
        return Collections.unmodifiableSet(mimeSubtypeSet);
    }

    private static Map<String, MediaType> getMediaTypeMap() {
        if (mediaTypeMap == null) {
            mediaTypeMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            for (MediaType mediaType : values()) {
                mediaTypeMap.put(mediaType.getMimeType(), mediaType);
            }
        }

        return mediaTypeMap;
    }

    public static MediaType getMediaType(String mimeType) {
        return getMediaTypeMap().get(mimeType.trim().split("/")[0].trim());
    }
}
