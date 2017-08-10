package com.chej.HandMate.Model;

/*
Gallery滑动效果的parcelable类
 */
import android.os.Parcel;
import android.os.Parcelable;

public class GalleryItems implements Parcelable{
    String name;
    String introduce;
    int image;

    public GalleryItems(String name, int image, String introduce) {
        this.name = name;
        this.image = image;
        this.introduce=introduce;
    }

    protected GalleryItems(Parcel in) {
        name = in.readString();
        image = in.readInt();
        introduce = in.readString();
    }

    public static final Creator<GalleryItems> CREATOR = new Creator<GalleryItems>() {
        @Override
        public GalleryItems createFromParcel(Parcel in) {
            return new GalleryItems(in);
        }

        @Override
        public GalleryItems[] newArray(int size) {
            return new GalleryItems[size];
        }
    };

    public String getName() {
        return name;
    }

    public int getImage() {
        return image;
    }
    public  String getIntroduce(){
        return introduce;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(image);
        dest.writeString(introduce);
    }
}
