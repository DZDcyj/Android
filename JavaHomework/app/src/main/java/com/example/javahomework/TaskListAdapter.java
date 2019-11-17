package com.example.javahomework;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.javahomework.Tasks.TaskList;

import java.util.List;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder> {

    private List<TaskList> taskLists;
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    public TaskListAdapter(List<TaskList> taskLists, Context context) {
        this.taskLists = taskLists;
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public TaskListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mLayoutInflater.inflate(R.layout.tasklist_item, parent, false));
    }

    @Override
    public void onBindViewHolder(TaskListAdapter.ViewHolder holder, int position) {
        holder.taskListName.setText(taskLists.get(position).getName());
        holder.taskListType.setText(taskLists.get(position).getType());
    }

    @Override
    public int getItemCount() {
        return taskLists == null ? 0 : taskLists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView taskListName;
        TextView taskListType;
        LinearLayout tasklistItem, tasklistHidden;

        public ViewHolder(final View itemView) {
            super(itemView);
            taskListName = itemView.findViewById(R.id.tasklist_name);
            taskListType = itemView.findViewById(R.id.tasklist_type);
            tasklistItem = itemView.findViewById(R.id.tasklist_item);
            tasklistHidden = itemView.findViewById(R.id.tasklist_item_hidden);
        }
    }
}
