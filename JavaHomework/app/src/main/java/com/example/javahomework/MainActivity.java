package com.example.javahomework;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.javahomework.Tasks.BaseTask;
import com.example.javahomework.Tasks.CycleTask;
import com.example.javahomework.Tasks.LongTask;
import com.example.javahomework.Tasks.SubTask;
import com.example.javahomework.Tasks.TaskList;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Context mContext;
    private ItemTouchHelper mItemTouchHelper;
    private SwipeRecyclerView mRecyclerView;
    private List<TaskList> taskLists;
    private FloatingActionButton fab;
    private String newTasklistName;
    private String newTaskListType;

    private boolean editMode = false;

    private TaskListAdapter taskListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LitePal.initialize(this);
        LitePal.getDatabase();
        initData();
        initRecyclerView();
    }

    private void initRecyclerView() {
        mRecyclerView = findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                    final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                            ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                    final int swipeFlags = 0;
                    return makeMovementFlags(dragFlags, swipeFlags);
                } else {
                    final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                    final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                    return makeMovementFlags(dragFlags, swipeFlags);
                }
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                //得到当拖拽的viewHolder的Position
                int fromPosition = viewHolder.getAdapterPosition();
                //拿到当前拖拽到的item的viewHolder
                int toPosition = target.getAdapterPosition();
                if (fromPosition < toPosition) {
                    for (int i = fromPosition; i < toPosition; i++) {
                        Collections.swap(taskLists, i, i + 1);
                    }
                } else {
                    for (int i = fromPosition; i > toPosition; i--) {
                        Collections.swap(taskLists, i, i - 1);
                    }
                }
                taskListAdapter.notifyItemMoved(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                int position = viewHolder.getAdapterPosition();
                taskListAdapter.notifyItemRemoved(position);
                TaskList taskList = taskLists.get(viewHolder.getLayoutPosition());
                taskLists.remove(position);
                taskList.delete();
            }
        });
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        mRecyclerView.setRightClickListener(new SwipeRecyclerView.OnRightClickListener() {
            @Override
            public void onRightClick(int position, String id) {

            }
        });
        mContext = MainActivity.this;
        taskListAdapter = new TaskListAdapter(taskLists, mContext);
        mRecyclerView.setAdapter(taskListAdapter);
        mRecyclerView.addOnItemTouchListener(new OnRecyclerItemClickListener(mRecyclerView) {
            @Override
            public void onItemClick(RecyclerView.ViewHolder vh) {
                final TaskList taskList = taskLists.get(vh.getLayoutPosition());
                if (!editMode) {
                    Intent intent = new Intent(MainActivity.this, TaskListDisplay.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("TaskList", taskList);
                    intent.putExtras(bundle);
                    startActivity(intent);
                } else {
                    editMode = false;
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            taskList.setName(newTasklistName);
                            taskList.setType(newTaskListType);
                            ContentValues contentValues = new ContentValues();
                            contentValues.put("name", newTasklistName);
                            LitePal.updateAll(TaskList.class, contentValues, "selfID=?", taskList.getSelfID());
                            contentValues.put("type", newTaskListType);
                            LitePal.updateAll(TaskList.class, contentValues, "selfID=?", taskList.getSelfID());
                            taskListAdapter.notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    View dialogView = View.inflate(mContext, R.layout.add_tasklist, null);
                    dialog.setView(dialogView);
                    final EditText name = dialogView.findViewById(R.id.new_tasklist_name);
                    final EditText type = dialogView.findViewById(R.id.new_tasklist_type);
                    name.setText(taskList.getName());
                    type.setText(taskList.getType());
                    name.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            newTasklistName = name.getText().toString();
                        }
                    });
                    type.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            newTaskListType = type.getText().toString();
                        }
                    });
                    newTasklistName = name.getText().toString();
                    newTaskListType = type.getText().toString();
                    dialog.setTitle("Edit tasklist");
                    dialog.show();
                }
            }

            @Override
            public void onItemLongClick(RecyclerView.ViewHolder vh) {
                if (vh.getLayoutPosition() != 0 && vh.getLayoutPosition() != 1) {
                    mItemTouchHelper.startDrag(vh);
                }
            }
        });
        fab = findViewById(R.id.add_new_tasklist);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TaskList taskList = new TaskList(newTasklistName, newTaskListType);
                        taskList.setSelfID(String.valueOf(System.currentTimeMillis()));
                        taskLists.add(taskList);
                        taskList.save();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog dialog = builder.create();
                View dialogView = View.inflate(mContext, R.layout.add_tasklist, null);
                dialog.setView(dialogView);
                final EditText name = dialogView.findViewById(R.id.new_tasklist_name);
                final EditText type = dialogView.findViewById(R.id.new_tasklist_type);
                name.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        newTasklistName = name.getText().toString();
                    }
                });
                type.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        newTaskListType = type.getText().toString();
                    }
                });
                dialog.setTitle("Create a new tasklist");
                dialog.show();
            }
        });

    }

    private void initData() {
        taskLists = LitePal.findAll(TaskList.class);
        if (taskLists == null)
            taskLists = new ArrayList<>();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_by_title:
                Collections.sort(taskLists, new Comparator<TaskList>() {
                    @Override
                    public int compare(TaskList o1, TaskList o2) {
                        return (o1.getName()).compareTo(o2.getName());
                    }
                });
                taskListAdapter.notifyDataSetChanged();
                break;
            case R.id.sort_by_type:
                Collections.sort(taskLists, new Comparator<TaskList>() {
                    @Override
                    public int compare(TaskList o1, TaskList o2) {
                        return (o1.getType()).compareTo(o2.getType());
                    }
                });
                taskListAdapter.notifyDataSetChanged();
                break;
            case R.id.edit_task_list:
                if (editMode) {
                    editMode = false;
                    Toast.makeText(mContext, "You closed the edit mode", Toast.LENGTH_SHORT).show();
                } else {
                    editMode = true;
                    Toast.makeText(mContext, "You opened the edit mode, choose a tasklist to edit", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.clear_cache:
                List<BaseTask> baseTasks = LitePal.findAll(BaseTask.class);
                for (BaseTask baseTask : baseTasks) {
                    if (LitePal.where("selfID=?", baseTask.getFatherID()).find(TaskList.class).isEmpty()) {
                        baseTask.delete();
                    }
                }
                List<CycleTask> cycleTasks = LitePal.findAll(CycleTask.class);
                for (CycleTask cycleTask : cycleTasks) {
                    if (LitePal.where("selfID=?", cycleTask.getFatherID()).find(TaskList.class).isEmpty()) {
                        cycleTask.delete();
                    }
                }
                List<LongTask> longTasks = LitePal.findAll(LongTask.class);
                for (LongTask longTask : longTasks) {
                    if (LitePal.where("selfID=?", longTask.getFatherID()).find(TaskList.class).isEmpty()) {
                        longTask.delete();
                    }
                }
                List<SubTask> subTasks = LitePal.findAll(SubTask.class);
                for (SubTask subTask : subTasks) {
                    if (LitePal.where("selfID=?", subTask.getFatherID()).find(SubTask.class).isEmpty()) {
                        if (LitePal.where("selfID=?", subTask.getFatherID()).find(LongTask.class).isEmpty()) {
                            subTask.delete();
                        }
                    }
                }
                Toast.makeText(mContext, "Successfully cleared the cache!", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
