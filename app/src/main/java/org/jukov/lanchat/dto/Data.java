package org.jukov.lanchat.dto;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.jukov.lanchat.util.Strings;

/**
 * Created by jukov on 22.02.2016.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PeopleData.class, name = "PeopleData"),
    @JsonSubTypes.Type(value = ChatData.class, name = "ChatData")
})
public abstract class Data {

    private String name;

    public Data() {
    }

    public Data(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        name = sharedPreferences.getString("name", Strings.DEFAULT_NAME);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
