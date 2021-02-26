package ru.ifsoft.network.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class PostInShared implements Parcelable, Serializable {
    private String post;
    private ArrayList<MediaItem> itemsList;

    public PostInShared() {
    }

    protected PostInShared(Parcel in) {
        post = in.readString();
        itemsList = in.createTypedArrayList(MediaItem.CREATOR);
    }

    public static final Creator<PostInShared> CREATOR = new Creator<PostInShared>() {
        @Override
        public PostInShared createFromParcel(Parcel in) {
            return new PostInShared(in);
        }

        @Override
        public PostInShared[] newArray(int size) {
            return new PostInShared[size];
        }
    };

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public ArrayList<MediaItem> getItemsList() {
        return itemsList;
    }

    public void setItemsList(ArrayList<MediaItem> itemsList) {
        this.itemsList = itemsList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(post);
        dest.writeTypedList(itemsList);
    }
}
