package com.chej.HandMate.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by samael on 2018/3/16.
 */

/**
 * 手套数据的辅助类
 */
public class GloveContant {
    public static final List<GloveItem> ITEMS = new ArrayList<GloveItem>();

    /**
     * A map of sample (Glove) items, by ID.
     */
    public static final Map<String, GloveItem> ITEM_MAP = new HashMap<String, GloveItem>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createGloveItem(i));
        }
    }

    private static void addItem(GloveItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static GloveItem createGloveItem(int position) {
        return new GloveItem(String.valueOf(position), "Item " + position, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A Glove item representing a piece of content.
     */
    public static class GloveItem {
        public final String id;
        public final String content;
        public final String details;

        public GloveItem(String id, String content, String details) {
            this.id = id;
            this.content = content;
            this.details = details;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
