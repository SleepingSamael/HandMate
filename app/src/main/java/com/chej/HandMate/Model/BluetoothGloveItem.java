package com.chej.HandMate.Model;

/**
 * Created by samael on 2018/3/19.
 */

public class BluetoothGloveItem {
        private String name;
        private int imageId;

        public BluetoothGloveItem(String name, int imageId) {
            this.name = name;
            this.imageId = imageId;
        }

        public String getName() {
            return name;
        }

        public int getImageId() {
            return imageId;
        }
    }