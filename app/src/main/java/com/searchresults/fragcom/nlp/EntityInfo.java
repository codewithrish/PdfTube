package com.searchresults.fragcom.nlp;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.VisibleForTesting;

import com.google.api.services.language.v1beta1.model.Entity;

import java.util.Map;

public class EntityInfo implements Parcelable {

    public static final Creator<EntityInfo> CREATOR = new Creator<EntityInfo>() {
        @Override
        public EntityInfo createFromParcel(Parcel in) {
            return new EntityInfo(in);
        }

        @Override
        public EntityInfo[] newArray(int size) {
            return new EntityInfo[size];
        }
    };

    @VisibleForTesting
    static final String KEY_WIKIPEDIA_URL = "wikipedia_url";

    /**
     * The representative name for the entity.
     */
    public final String name;

    /**
     * The entity type.
     */
    public final String type;

    /**
     * The salience score associated with the entity in the [0, 1.0] range.
     */
    public final float salience;

    /**
     * The Wikipedia URL.
     */
    public final String wikipediaUrl;

    public EntityInfo(Entity entity) {
        name = entity.getName();
        type = entity.getType();
        salience = entity.getSalience();
        final Map<String, String> metadata = entity.getMetadata();
        if (metadata != null && metadata.containsKey(KEY_WIKIPEDIA_URL)) {
            wikipediaUrl = metadata.get(KEY_WIKIPEDIA_URL);
        } else {
            wikipediaUrl = null;
        }
    }

    protected EntityInfo(Parcel in) {
        name = in.readString();
        type = in.readString();
        salience = in.readFloat();
        wikipediaUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeString(type);
        out.writeFloat(salience);
        out.writeString(wikipediaUrl);
    }

}