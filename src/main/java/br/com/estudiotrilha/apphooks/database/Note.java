package br.com.estudiotrilha.apphooks.database;

import android.content.Context;

import com.mauriciogiordano.easydb.bean.AbstractBean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mauricio on 12/7/14.
 */
public class Note extends AbstractBean {

    private String id = "";
    private String title = "";
    private String message = "";

    public Note() {
        super(Note.class, true);
    }

    public Note(Context context) {
        super(Note.class, true, context);
    }

    public static Note instanceFromJson(JSONObject json, Context context) {
        Note obj = new Note(context);

        try {
            obj.setId(String.valueOf(json.hashCode()));
            obj.setTitle(json.getString("title"));
            obj.setMessage(json.getString("message"));
        } catch(JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }

    @Override
    public JSONObject toJson() {
        JSONObject object = new JSONObject();

        try {
            object.put("id", id);
            object.put("title", title);
            object.put("message", message);
        } catch(JSONException e) {
            e.printStackTrace();
        }

        return object;
    }

    @Override
    public Note fromJson(JSONObject json) {
        return Note.instanceFromJson(json, context);
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
