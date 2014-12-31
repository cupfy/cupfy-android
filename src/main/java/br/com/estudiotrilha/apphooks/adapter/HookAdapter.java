package br.com.estudiotrilha.apphooks.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;

import android.widget.CheckBox;

import java.util.List;

import br.com.estudiotrilha.apphooks.R;
import br.com.estudiotrilha.apphooks.database.Namespace;

/**
 * Created by mauricio on 12/30/14.
 */
public class HookAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private List<Namespace> dataSet;

    public HookAdapter(LayoutInflater layoutInflater, List<Namespace> dataSet) {
        this.layoutInflater = layoutInflater;
        this.dataSet = dataSet;
    }

    public static class ViewHolder {
        public Namespace namespace;
        public TextView textNamespace;
        public TextView textStatus;
        public CheckBox checkActivated;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final ViewHolder viewHolder;

        if(view == null) {
            view = layoutInflater.inflate(R.layout.list_hook, null);

            viewHolder = new ViewHolder();
            viewHolder.textNamespace = (TextView) view.findViewById(R.id.textNamespace);
            viewHolder.textStatus = (TextView) view.findViewById(R.id.textStatus);
            viewHolder.checkActivated = (CheckBox) view.findViewById(R.id.checkActivated);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.checkActivated.setChecked(false);

        viewHolder.namespace = dataSet.get(i);

        viewHolder.textNamespace.setText(viewHolder.namespace.getNamespace());

        if(!viewHolder.namespace.isApproved()) {
            viewHolder.textStatus.setText(R.string.text_waiting_for_approval);
            viewHolder.checkActivated.setEnabled(false);
            viewHolder.checkActivated.setActivated(false);
        } else {
            viewHolder.checkActivated.setEnabled(true);
            viewHolder.checkActivated.setChecked(viewHolder.namespace.isActivated());

            viewHolder.textStatus.setText(viewHolder.namespace.isActivated() ? R.string.text_activated : R.string.text_deactivated);
        }

        viewHolder.checkActivated.setTag(viewHolder);

        viewHolder.checkActivated.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if(viewHolder.namespace.isApproved()) {
                    ViewHolder viewHolder = (ViewHolder) compoundButton.getTag();

                    viewHolder.namespace.setActivated(checked);
                    viewHolder.namespace.save();
                    viewHolder.textStatus.setText(viewHolder.namespace.isActivated() ? R.string.text_activated : R.string.text_deactivated);
                } else {
                    viewHolder.checkActivated.setChecked(false);
                }
            }
        });

        return view;
    }

    public void setDataSet(List<Namespace> dataSet) {
        this.dataSet = dataSet;
    }

    @Override
    public int getCount() {
        return dataSet.size();
    }

    @Override
    public Object getItem(int i) {
        return dataSet.get(i);
    }

    @Override
    public long getItemId(int i) {
        return (long) i;
    }
}
