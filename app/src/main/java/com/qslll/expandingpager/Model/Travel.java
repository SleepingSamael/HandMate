package com.qslll.expandingpager.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class Travel implements Parcelable{
    String name;
    String introduce;
    int image;

    public Travel(String name, int image,String introduce) {
        this.name = name;
        this.image = image;
        this.introduce=introduce;
    }

    protected Travel(Parcel in) {
        name = in.readString();
        image = in.readInt();
        introduce = in.readString();
    }

    public static final Creator<Travel> CREATOR = new Creator<Travel>() {
        @Override
        public Travel createFromParcel(Parcel in) {
            return new Travel(in);
        }

        @Override
        public Travel[] newArray(int size) {
            return new Travel[size];
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
