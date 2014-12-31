package br.com.estudiotrilha.apphooks.database;

import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import com.mauriciogiordano.easydb.bean.AbstractHasManyBean;
import com.mauriciogiordano.easydb.helper.JSONArray;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import br.com.estudiotrilha.apphooks.R;
import br.com.estudiotrilha.apphooks.network.Delegate;
import br.com.estudiotrilha.apphooks.network.Endpoints;
import br.com.estudiotrilha.apphooks.network.HttpClientHelper;
import br.com.estudiotrilha.apphooks.network.Network;

/**
 * Created by mauricio on 12/7/14.
 */
public class Namespace extends AbstractHasManyBean {

    private String _id = "";
    private String namespace = "";
    private boolean approved = false;
    private boolean activated = true;

    public Namespace() {
        super(Namespace.class, Note.class, true);
    }

    public Namespace(Context context) {
        super(Namespace.class, Note.class, true, context);
    }

    public static Namespace find(String id, Context context) {
        Namespace instance = new Namespace(context);

        return (Namespace) instance.find(id);
    }

    public static List<Namespace> findAll(Context context) {
        Namespace instance = new Namespace(context);

        return instance.findAll();
    }

    public static boolean isNamespaceActivated(String namespace, Context context) {
        List<Namespace> namespaceList = findAll(context);

        for(int i=0; i<namespaceList.size(); i++) {
            if(namespaceList.get(i).namespace.equals(namespace)) {
                return namespaceList.get(i).isActivated();
            }
        }

        return false;
    }

    public static void attachHook(String namespace, final Context context, final OnAttachHook onAttachHook) {
        HttpClientHelper client = new HttpClientHelper(Endpoints.HOST, Endpoints.Device.ATTACH_HOOK, false, context);

        //client.addParamForGet("pushId", gcm.getRegistrationId(this));
        client.addParamForPost("pushId", "sagasgqtt12521rasgasgas");
        client.addParamForPost("namespace", namespace);

        Network.newRequest(client, Network.POST, new Delegate() {
            @Override
            public void requestResults(Network.Status status) {
                boolean err = false;

                Namespace namespace = null;

                if (status.hasInternet) {
                    if (status.response.getStatusLine().getStatusCode() != 200) {
                        err = true;
                    } else {
                        try {
                            namespace = Namespace.fromJson(new JSONObject(status.result), context);

                            namespace.save();
                        } catch(JSONException e) {
                            e.printStackTrace();
                            err = true;
                        }
                    }
                } else {
                    err = true;
                    Toast.makeText(context, R.string.toast_no_internet, Toast.LENGTH_LONG).show();
                }

                onAttachHook.deliver(namespace, status, err);
            }
        });
    }

    public static void loadHooks(final Context context, final OnLoadHooks onLoadHooks) {
        HttpClientHelper client = new HttpClientHelper(Endpoints.HOST, Endpoints.Device.GET_HOOKS, false, context);

        //client.addParamForGet("pushId", gcm.getRegistrationId(this));
        client.addParamForGet("pushId", "sagasgqtt12521rasgasgas");

        Network.newRequest(client, Network.GET, new Delegate() {
            @Override
            public void requestResults(Network.Status status) {
                boolean err = false;

                List<Namespace> namespaceList = new ArrayList<Namespace>();

                if (status.hasInternet) {
                    if (status.response.getStatusLine().getStatusCode() != 200) {
                        err = true;
                    } else {
                        try {
                            JSONArray hooks = new JSONArray(status.result);

                            for(int i=0; i<hooks.length(); i++) {
                                Namespace namespace = Namespace.fromJson(hooks.getJSONObject(i), context);;

                                Namespace cache = Namespace.find(hooks.getJSONObject(i).getString("_id"), context);

                                if(cache != null) {
                                    namespace.setActivated(cache.isActivated());
                                }

                                namespace.save();

                                if(hooks.getJSONObject(i).optBoolean("removed", false)) {
                                    namespace.remove();
                                } else {
                                    namespaceList.add(namespace);
                                }
                            }
                        } catch(JSONException e) {
                            e.printStackTrace();
                            err = true;
                        }
                    }
                } else {
                    err = true;
                    Toast.makeText(context, R.string.toast_no_internet, Toast.LENGTH_LONG).show();
                }

                onLoadHooks.deliver(namespaceList, status, err);
            }
        });
    }

    public static void registerDeviceAndLoadHooks(String name, final Context context, final OnLoadHooks onLoadHooks) {
        HttpClientHelper client = new HttpClientHelper(Endpoints.HOST, Endpoints.Device.REGISTER, false, context);

        client.addParamForPost("name", name);
        client.addParamForPost("model", getDeviceModel());
        client.addParamForPost("type", "0");
        client.addParamForPost("pushId", "sagasgqtt12521rasgasgas");

        Network.newRequest(client, Network.POST, new Delegate() {
            @Override
            public void requestResults(Network.Status status) {
                if (status.hasInternet) {
                    loadHooks(context, onLoadHooks);
                } else {
                    Toast.makeText(context, R.string.toast_no_internet, Toast.LENGTH_LONG).show();
                    onLoadHooks.deliver(null, null, true);
                }
            }
        });
    }

    public static String getDeviceModel() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static Namespace fromJson(JSONObject json, Context context) {
        Namespace dummy = new Namespace(context);

        return (Namespace) dummy.fromJson(json);
    }

    @Override
    public String getId() {
        return _id;
    }

    public void setId(String id) {
        _id = id;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public static interface OnAttachHook {
        public void deliver(Namespace namespace, Network.Status status, boolean err);
    }

    public static interface OnLoadHooks {
        public void deliver(List<Namespace> namespaceList, Network.Status status, boolean err);
    }
}
