package com.droidsonroids.bootcamp.soccertablemanager.adapter;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.droidsonroids.bootcamp.soccertablemanager.Const;
import com.droidsonroids.bootcamp.soccertablemanager.R;
import com.droidsonroids.bootcamp.soccertablemanager.api.model.Table;
import com.droidsonroids.bootcamp.soccertablemanager.event.JoinRequestEvent;
import com.droidsonroids.bootcamp.soccertablemanager.event.LeaveRequestEvent;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;


public class TableAdapter extends RecyclerView.Adapter<TableAdapter.TableVieHolder> {

    List<Table> tableList;

    public TableAdapter(List<Table> tableList) {
        this.tableList = tableList;
    }


    @Override
    public TableVieHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View viewItem = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.table_cv, viewGroup, false);
        return new TableVieHolder(viewItem);
    }

    @Override
    public void onBindViewHolder(TableVieHolder tableVieHolder, int i) {
        Table table = tableList.get(i);
        tableVieHolder.tableIdTextView.setText("Table id: " + table.getTableId() + " time: " + table.getTime());
        tableVieHolder.tableFreeSpotsTextView.setText("Free spots: " + table.getFreeSpotsNumber());
        String usersString = "";
        for (String user : table.getUserNameList()) {
            usersString += user + " ";
        }
        tableVieHolder.userListTextView.setText(usersString);
    }

    @Override
    public int getItemCount() {
        return tableList.size();
    }


    class TableVieHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Bind(R.id.table_id_tv)
        TextView tableIdTextView;
        @Bind(R.id.free_spots_tv)
        TextView tableFreeSpotsTextView;
        @Bind(R.id.user_list_tv)
        TextView userListTextView;

        public TableVieHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(itemView.getContext());
            int userId = sharedPreferences.getInt(Const.USER_ID, 0);
            Table table = tableList.get(getAdapterPosition());
            String currentUser = sharedPreferences.getString(Const.USER_NAME, "");
            if (isUserAlreadyOnList(currentUser, table)) {
                showDialog(table.getTableId(), userId);
            } else {
                EventBus.getDefault().post(new JoinRequestEvent(table.getTableId(), userId));
            }


        }

        public boolean isUserAlreadyOnList(String user, Table table) {
            for (String userName : table.getUserNameList()) {
                if (userName.equals(user)) {
                    return true;
                }
            }
            return false;
        }

        public void showDialog(final int tabelId, final int userId) {
            AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
            builder.setMessage("Are you sure?");
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    EventBus.getDefault().post(new LeaveRequestEvent(tabelId, userId));
                }
            });
            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}
