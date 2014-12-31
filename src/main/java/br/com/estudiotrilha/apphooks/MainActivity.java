package br.com.estudiotrilha.apphooks;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.gc.materialdesign.views.ButtonRectangle;

import java.util.ArrayList;
import java.util.List;

import br.com.estudiotrilha.apphooks.adapter.HookAdapter;
import br.com.estudiotrilha.apphooks.database.Namespace;
import br.com.estudiotrilha.apphooks.helper.SwipeRefreshLayout;
import br.com.estudiotrilha.apphooks.helper.Util;
import br.com.estudiotrilha.apphooks.network.Network;


public class MainActivity extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener {

    private TextView textNoHooks;
    private ListView listHooks;
    private ButtonRectangle buttonAddHook;

    private SwipeRefreshLayout swipeRefresh;

    private HookAdapter adapter;

    private static class AdRegister {
        public static AlertDialog dialog;
        public static EditText editNamespace;
    }

    private static class AdName {
        public static AlertDialog dialog;
        public static EditText editName;
    }

    private void setName(String name) {
        getSharedPreferences("deviceInfo", MODE_PRIVATE)
                .edit()
                .putString("name", name)
                .commit();
    }

    private String getName() {
        return getSharedPreferences("deviceInfo", MODE_PRIVATE)
                .getString("name", null);
    }

    private boolean hasName() {
        return getName() != null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Util.configureGcm(this);

        textNoHooks = (TextView) findViewById(R.id.textNoHooks);
        listHooks = (ListView) findViewById(R.id.list);
        buttonAddHook = (ButtonRectangle) findViewById(R.id.buttonAddHook);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        adapter = new HookAdapter(getLayoutInflater(), new ArrayList<Namespace>());

        listHooks.setAdapter(adapter);
        listHooks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                HookAdapter.ViewHolder viewHolder = (HookAdapter.ViewHolder) view.getTag();

                viewHolder.checkActivated.toggle();
            }
        });

        buttonAddHook.setClickAfterRipple(false);

        swipeRefresh.setOnRefreshListener(this);
        swipeRefresh.setColorSchemeColors(0xff3498db, 0xffe74c3c, 0xfff1c40f, 0xff2ecc71);
        swipeRefresh.setScrollView(listHooks);

        buttonAddHook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AdRegister.dialog.show();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_register_device, null);

        AdRegister.editNamespace = (EditText) dialogView.findViewById(R.id.editNamespace);

        AdRegister.dialog = builder.setTitle(R.string.ad_title_attach_hook)
                .setView(dialogView)
                .setPositiveButton(R.string.ad_button_register, null)
                .setNegativeButton(R.string.ad_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AdRegister.dialog.dismiss();
                    }
                })
                .create();

        AdRegister.dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button positive = AdRegister.dialog.getButton(AlertDialog.BUTTON_POSITIVE);

                positive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(AdRegister.editNamespace.length() < 1) {
                            AdRegister.editNamespace.setError(getString(R.string.error_required));
                        } else if(AdRegister.editNamespace.length() < 3) {
                            AdRegister.editNamespace.setError(getString(R.string.error_namespace_too_short));
                        } else {
                            swipeRefresh.setRefreshing(true);
                            Namespace.attachHook(AdRegister.editNamespace.getText().toString(), MainActivity.this, new Namespace.OnAttachHook() {
                                @Override
                                public void deliver(Namespace namespace, Network.Status status, boolean err) {
                                    swipeRefresh.setRefreshing(false);

                                    if(err) {
                                        AdRegister.dialog.show();

                                        if(status.response.getStatusLine().getStatusCode() == 404) {
                                            AdRegister.editNamespace.setError(getString(R.string.error_namespace_not_found));
                                        } else if(status.response.getStatusLine().getStatusCode() == 409) {
                                            AdRegister.editNamespace.setError(getString(R.string.error_namespace_already_hooked));
                                        } else {
                                            AdRegister.editNamespace.setError(getString(R.string.error_required));
                                        }
                                    } else {
                                        refreshNamespaces();
                                    }
                                }
                            });

                            AdRegister.dialog.dismiss();
                        }
                    }
                });
            }
        });

        AdRegister.dialog.setCancelable(false);

        if(!hasName()) {
            builder = new AlertDialog.Builder(this);

            View nameView = getLayoutInflater().inflate(R.layout.dialog_register_name, null);

            AdName.editName = (EditText) nameView.findViewById(R.id.editName);

            AdName.dialog = builder.setTitle(R.string.ad_title_set_name)
                    .setView(nameView)
                    .setPositiveButton(R.string.ad_button_ok, null)
                    .create();

            AdName.dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    Button positive = AdName.dialog.getButton(AlertDialog.BUTTON_POSITIVE);

                    positive.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (AdName.editName.length() < 1) {
                                AdName.editName.setError(getString(R.string.error_required));
                            } else {
                                setName(AdName.editName.getText().toString());

                                AdName.dialog.dismiss();
                                resumeInit();
                            }
                        }
                    });
                }
            });

            AdName.dialog.setCancelable(false);

            AdName.dialog.show();
        } else {
            resumeInit();
        }
    }

    public void resumeInit() {

        if(Namespace.findAll(this).size() == 0) {
            textNoHooks.setVisibility(View.GONE);
            listHooks.setVisibility(View.GONE);
            buttonAddHook.setVisibility(View.GONE);
        } else {
            textNoHooks.setVisibility(View.GONE);
            listHooks.setVisibility(View.VISIBLE);
        }

        refreshNamespaces();

    }

    @Override
    public void onRefresh() {
        refreshNamespaces();
    }

    private void refreshNamespaces() {
        swipeRefresh.setRefreshing(true);
        Namespace.loadHooks(this, new Namespace.OnLoadHooks() {
            @Override
            public void deliver(List<Namespace> namespaceList, Network.Status status, boolean err) {
                if (err && status != null) {
                    Namespace.registerDeviceAndLoadHooks(getName(), MainActivity.this, this);
                } else if (err && status == null) {
                    namespaceList = Namespace.findAll(MainActivity.this);
                    err = false;
                }

                if (!err) {
                    if (namespaceList.size() == 0) {
                        textNoHooks.setVisibility(View.VISIBLE);
                        listHooks.setVisibility(View.GONE);
                    } else {
                        textNoHooks.setVisibility(View.GONE);
                        listHooks.setVisibility(View.VISIBLE);

                        adapter.setDataSet(namespaceList);
                        adapter.notifyDataSetChanged();
                    }

                    buttonAddHook.setVisibility(View.VISIBLE);
                    swipeRefresh.setRefreshing(false);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
