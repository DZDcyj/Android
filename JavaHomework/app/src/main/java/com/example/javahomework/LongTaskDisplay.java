package com.example.javahomework;

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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.javahomework.Tasks.BaseTask;
import com.example.javahomework.Tasks.LongTask;
import com.example.javahomework.Tasks.SubTask;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LongTaskDisplay extends AppCompatActivity {

    private SwipeRecyclerViewForTask swipeRecyclerView;
    private TextView displayName;
    private String taskName;
    private List<BaseTask> subTasks;
    private FloatingActionButton newTask;
    private Context mContext;
    private String thisID;

    private TaskAdapter taskAdapter;
    private ItemTouchHelper mItemTouchHelper;

    private String newTaskName = "Unnamed";
    private String newTaskDescription = "No description";
    private String newTaskDeadline = "Deadline unset";

    private String queryString;

    private SearchView mSearchView;

    private boolean editSubTask = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_long_task_display);
        initData();
        initDisplay();

        mSearchView = findViewById(R.id.searchView_longTask);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                List<BaseTask> resource = new ArrayList<>();
                resource.addAll(LitePal.where("fatherID=?", thisID).find(SubTask.class));
                List<BaseTask> queryResult = new ArrayList<>();
                for (BaseTask task : resource) {
                    if (task.getName().equals(queryString)) {
                        queryResult.add(task);
                    }
                }
                final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                View view = View.inflate(mContext, R.layout.choose_tasklist, null);
                final SwipeRecyclerView swipeRecyclerView = view.findViewById(R.id.choose_tasklists);
                swipeRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                swipeRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
                swipeRecyclerView.setAdapter(new TaskAdapter(queryResult, mContext));
                AlertDialog alertDialog = builder.create();
                alertDialog.setView(view);
                alertDialog.setTitle("Query Result");
                alertDialog.show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                queryString = newText;
                return false;
            }
        });
    }

    private void initData() {
        Intent intent = getIntent();
        thisID = intent.getStringExtra("longTaskID");
        List<SubTask> subTasks2 = LitePal.where("fatherID=?", thisID).find(SubTask.class);
        subTasks = new ArrayList<>();
        subTasks.addAll(subTasks2);
        taskName = intent.getStringExtra("longTaskName");
    }

    private void initDisplay() {
        mContext = LongTaskDisplay.this;
        taskAdapter = new TaskAdapter(subTasks, mContext);
        displayName = findViewById(R.id.display_long_task_name);
        swipeRecyclerView = findViewById(R.id.longtaskrecyclerview);
        swipeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        swipeRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
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
                        Collections.swap(subTasks, i, i + 1);
                    }
                } else {
                    for (int i = fromPosition; i > toPosition; i--) {
                        Collections.swap(subTasks, i, i - 1);
                    }
                }
                taskAdapter.notifyItemMoved(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                int position = viewHolder.getAdapterPosition();
                BaseTask task = subTasks.get(viewHolder.getLayoutPosition());
                task.delete();
                subTasks.remove(position);
                taskAdapter.notifyDataSetChanged();
            }
        });
        swipeRecyclerView.setRightClickListener(new SwipeRecyclerViewForTask.OnRightClickListener() {
            @Override
            public void onRightClick(int position, String id) {

            }
        });
        mItemTouchHelper.attachToRecyclerView(swipeRecyclerView);
        swipeRecyclerView.addOnItemTouchListener(new OnRecyclerItemClickListener(swipeRecyclerView) {
            @Override
            public void onItemClick(RecyclerView.ViewHolder vh) {
                if (editSubTask) {
                    BaseTask subTask = subTasks.get(vh.getLayoutPosition());
                    Intent intent = new Intent(LongTaskDisplay.this, LongTaskDisplay.class);
                    intent.putExtra("longTaskID", subTask.getSelfID());
                    intent.putExtra("longTaskName", subTask.getName());
                    startActivity(intent);
                    editSubTask = false;
                }
            }

            @Override
            public void onItemLongClick(RecyclerView.ViewHolder vh) {

            }
        });
        swipeRecyclerView.setAdapter(taskAdapter);
        displayName.setText(taskName);
        newTask = findViewById(R.id.add_new_subTask);
        newTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SubTask subTask = new SubTask();
                        subTask.setName(newTaskName);
                        subTask.setDeadline(newTaskDeadline);
                        subTask.setStatus(BaseTask.UNFINISHED);
                        subTask.setDescription(newTaskDescription);
                        subTask.setFatherID(thisID);
                        subTask.setSelfID(String.valueOf(System.currentTimeMillis()));
                        subTasks.add(subTask);
                        subTask.save();
                        taskAdapter.notifyDataSetChanged();
                        newTaskDeadline = "Deadline unset";
                        newTaskName = "Unnamed";
                        newTaskDescription = "No description";
                        dialog.dismiss();
                    }
                });
                View subView = View.inflate(mContext, R.layout.create_long_task, null);
                AlertDialog createSubTaskDialog = builder.create();
                createSubTaskDialog.setView(subView);
                final EditText name = subView.findViewById(R.id.new_long_task_name);
                final EditText description = subView.findViewById(R.id.new_long_task_description);
                final TextView deadline = subView.findViewById(R.id.new_long_task_deadline);
                final ImageView deadlineChoose = subView.findViewById(R.id.new_long_task_deadline_choose);
                name.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        newTaskName = name.getText().toString();
                    }
                });
                description.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        newTaskDescription = description.getText().toString();
                    }
                });
                deadlineChoose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder datePickerBuilder = new AlertDialog.Builder(mContext);
                        datePickerBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                newTaskDeadline = deadline.getText().toString();
                            }
                        });
                        datePickerBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        View datePickerView = View.inflate(mContext, R.layout.date_picker, null);
                        AlertDialog datePickerDialog = datePickerBuilder.create();
                        datePickerDialog.setView(datePickerView);
                        int initYear, initMonth, initDay;
                        if (deadline.getText().toString().equals("Deadline unset")) {
                            Calendar calendar = Calendar.getInstance();
                            initYear = calendar.get(Calendar.YEAR);
                            initMonth = calendar.get(Calendar.MONTH);
                            initDay = calendar.get(Calendar.DAY_OF_MONTH);
                        } else {
                            String[] dates = deadline.getText().toString().split("-");
                            initYear = Integer.parseInt(dates[0]);
                            initMonth = Integer.parseInt(dates[1]) - 1;
                            initDay = Integer.parseInt(dates[2]);
                        }
                        final DatePicker datePicker = datePickerView.findViewById(R.id.date_picker);
                        datePicker.init(initYear, initMonth, initDay, new DatePicker.OnDateChangedListener() {
                            @Override
                            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                String date = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                                deadline.setText(date);
                            }
                        });
                        datePickerDialog.show();
                    }
                });
                createSubTaskDialog.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.activity_long_task_display, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.subtask_sort_by_name:
                Collections.sort(subTasks, new Comparator<BaseTask>() {
                    @Override
                    public int compare(BaseTask o1, BaseTask o2) {
                        return (o1.getName()).compareTo(o2.getName());
                    }
                });
                taskAdapter.notifyDataSetChanged();
                break;
            case R.id.subtask_sort_by_deadline:
                Collections.sort(subTasks, new Comparator<BaseTask>() {
                    @Override
                    public int compare(BaseTask o1, BaseTask o2) {
                        return (o1.getDeadline()).compareTo(o2.getDeadline());
                    }
                });
                taskAdapter.notifyDataSetChanged();
                break;
            case R.id.subtask_sort_by_status:
                Collections.sort(subTasks, new Comparator<BaseTask>() {
                    @Override
                    public int compare(BaseTask o1, BaseTask o2) {
                        return o1.getStatus() - o2.getStatus();
                    }
                });
                taskAdapter.notifyDataSetChanged();
                break;
            case R.id.manage_subtask:
                Toast.makeText(mContext, "Click a SubTask to manage its subtasks", Toast.LENGTH_SHORT).show();
                editSubTask = true;
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
