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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.javahomework.Tasks.BaseTask;
import com.example.javahomework.Tasks.CycleTask;
import com.example.javahomework.Tasks.LongTask;
import com.example.javahomework.Tasks.SubTask;
import com.example.javahomework.Tasks.TaskList;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class TaskListDisplay extends AppCompatActivity {
    private SwipeRecyclerViewForTask swipeRecyclerView;
    private TextView displayName;
    private String tasklistName;
    private List<BaseTask> tasks;
    private TaskList taskList;
    private FloatingActionButton newTask;
    private Context mContext;
    private String taskListID;

    private TaskAdapter taskAdapter;
    private ItemTouchHelper mItemTouchHelper;

    private String newTaskName = "Unnamed";
    private String newTaskDescription = "No description";
    private String newTaskDeadline = "Deadline unset";
    private String newCycleTaskCycleDays = "0";
    private int newCycleTaskTimes;

    private String queryString;

    private boolean copyMode = false;
    private boolean moveMode = false;

    private boolean editLongTask = false;

    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list_display);
        initData();
        initDisplay();
        mSearchView = findViewById(R.id.searchView);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                List<BaseTask> result = new ArrayList<>();
                result.addAll(LitePal.where("fatherID=?", taskListID).find(BaseTask.class));
                result.addAll(LitePal.where("fatherID=?", taskListID).find(CycleTask.class));
                result.addAll(LitePal.where("fatherID=?", taskListID).find(LongTask.class));
                List<BaseTask> queryResult = new ArrayList<>();
                for (BaseTask task : result) {
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
        taskList = (TaskList) intent.getSerializableExtra("TaskList");
        taskListID = taskList.getSelfID();
        tasks = LitePal.where("fatherID=?", taskListID).find(BaseTask.class);
        List<CycleTask> cycleTasks = LitePal.where("fatherID=?", taskListID).find(CycleTask.class);
        List<LongTask> longTasks = LitePal.where("fatherID=?", taskListID).find(LongTask.class);
        tasks.addAll(cycleTasks);
        tasks.addAll(longTasks);
        tasklistName = taskList.getName();
    }

    private void initDisplay() {
        mContext = TaskListDisplay.this;
        taskAdapter = new TaskAdapter(tasks, mContext);
        displayName = findViewById(R.id.display_tasklist_name);
        swipeRecyclerView = findViewById(R.id.taskrecyclerview);
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
                        Collections.swap(tasks, i, i + 1);
                    }
                } else {
                    for (int i = fromPosition; i > toPosition; i--) {
                        Collections.swap(tasks, i, i - 1);
                    }
                }
                taskAdapter.notifyItemMoved(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                int position = viewHolder.getAdapterPosition();
                BaseTask task = tasks.get(viewHolder.getLayoutPosition());
                task.delete();
                tasks.remove(position);
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
                final BaseTask source = tasks.get(vh.getLayoutPosition());
                if (copyMode) {
                    moveMode = false;
                    copyMode = false;
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    View view = View.inflate(mContext, R.layout.choose_tasklist, null);
                    final AlertDialog alertDialog = builder.create();
                    alertDialog.setView(view);
                    final List<TaskList> taskLists = LitePal.findAll(TaskList.class);
                    final TaskListAdapter taskListAdapter = new TaskListAdapter(taskLists, mContext);
                    final SwipeRecyclerView swipeRecyclerView = view.findViewById(R.id.choose_tasklists);
                    swipeRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                    swipeRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
                    swipeRecyclerView.addOnItemTouchListener(new OnRecyclerItemClickListener(swipeRecyclerView) {
                        @Override
                        public void onItemClick(RecyclerView.ViewHolder vh) {
                            int position = vh.getAdapterPosition();
                            TaskList target = taskLists.get(position);
                            if (source instanceof LongTask) {
                                LongTask longTask = new LongTask();
                                longTask.setFatherID(target.getSelfID());
                                longTask.setName(source.getName());
                                longTask.setDescription(source.getDescription());
                                longTask.setDeadline(source.getDeadline());
                                longTask.setSelfID(String.valueOf(System.currentTimeMillis()));
                                longTask.setStatus(source.getStatus());
                                longTask.save();
                                String originID = source.getSelfID();
                                String newID = longTask.getSelfID();
                                Queue<String> subTasks = new LinkedList<>();
                                subTasks.offer(originID);
                                subTasks.offer(newID);
                                do {
                                    originID = subTasks.poll();
                                    newID = subTasks.poll();
                                    List<SubTask> subTaskList;
                                    subTaskList = LitePal.where("fatherID=?", originID).find(SubTask.class);
                                    for (SubTask subTask : subTaskList) {
                                        subTasks.offer(subTask.getSelfID());
                                        SubTask task = new SubTask();
                                        task.setName(subTask.getName());
                                        task.setStatus(subTask.getStatus());
                                        task.setFatherID(newID);
                                        task.setSelfID(String.valueOf(System.currentTimeMillis()));
                                        subTasks.offer(task.getSelfID());
                                        task.setDescription(subTask.getDescription());
                                        task.setDeadline(subTask.getDeadline());
                                        task.save();
                                    }
                                } while (!subTasks.isEmpty());
                                taskListAdapter.notifyDataSetChanged();
                            } else {
                                if (source instanceof CycleTask) {
                                    CycleTask cycleTask = new CycleTask();
                                    cycleTask.setFatherID(target.getSelfID());
                                    cycleTask.setName(source.getName());
                                    cycleTask.setDescription(source.getDescription());
                                    cycleTask.setTimes(((CycleTask) source).getTimes());
                                    cycleTask.setCycleDays(((CycleTask) source).getCycleDays());
                                    cycleTask.setSelfID(String.valueOf(System.currentTimeMillis()));
                                    cycleTask.setDeadline(source.getDeadline());
                                    cycleTask.setStatus(source.getStatus());
                                    cycleTask.save();
                                    taskAdapter.notifyDataSetChanged();
                                } else {
                                    BaseTask baseTask = new BaseTask();
                                    baseTask.setFatherID(target.getSelfID());
                                    baseTask.setName(source.getName());
                                    baseTask.setDescription(source.getDescription());
                                    baseTask.setDeadline(source.getDeadline());
                                    baseTask.setStatus(source.getStatus());
                                    baseTask.setSelfID(String.valueOf(System.currentTimeMillis()));
                                    baseTask.save();
                                    taskAdapter.notifyDataSetChanged();
                                }
                            }
                            alertDialog.dismiss();
                        }

                        @Override
                        public void onItemLongClick(RecyclerView.ViewHolder vh) {

                        }
                    });
                    swipeRecyclerView.setAdapter(taskListAdapter);
                    alertDialog.setTitle("Choose the target Tasklist");
                    alertDialog.show();
                } else if (moveMode) {
                    moveMode = false;
                    copyMode = false;
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    View view = View.inflate(mContext, R.layout.choose_tasklist, null);
                    final AlertDialog alertDialog = builder.create();
                    alertDialog.setView(view);
                    final List<TaskList> taskLists = LitePal.findAll(TaskList.class);
                    final TaskListAdapter taskListAdapter = new TaskListAdapter(taskLists, mContext);
                    final SwipeRecyclerView swipeRecyclerView = view.findViewById(R.id.choose_tasklists);
                    swipeRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                    swipeRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
                    swipeRecyclerView.addOnItemTouchListener(new OnRecyclerItemClickListener(swipeRecyclerView) {
                        @Override
                        public void onItemClick(RecyclerView.ViewHolder vh) {
                            int position = vh.getAdapterPosition();
                            TaskList target = taskLists.get(position);
                            if (source.getFatherID().equals(target.getSelfID())) {
                                alertDialog.dismiss();
                            } else {
                                source.setFatherID(target.getSelfID());
                                ContentValues contentValues = new ContentValues();
                                contentValues.put("fatherID", target.getSelfID());
                                if (source instanceof LongTask)
                                    LitePal.updateAll(LongTask.class, contentValues, "selfID=?", source.getSelfID());
                                else if (source instanceof CycleTask)
                                    LitePal.updateAll(CycleTask.class, contentValues, "selfID=?", source.getSelfID());
                                else
                                    LitePal.updateAll(BaseTask.class, contentValues, "selfID=?", source.getSelfID());
                                tasks.remove(source);
                                taskAdapter.notifyDataSetChanged();
                                alertDialog.dismiss();
                            }
                        }

                        @Override
                        public void onItemLongClick(RecyclerView.ViewHolder vh) {

                        }
                    });
                    swipeRecyclerView.setAdapter(taskListAdapter);
                    alertDialog.setTitle("Choose the target Tasklist");
                    alertDialog.show();
                } else if (editLongTask) {
                    if (source instanceof LongTask) {
                        LongTask longTask = (LongTask) source;
                        Intent intent = new Intent(TaskListDisplay.this, LongTaskDisplay.class);
                        intent.putExtra("longTaskID", longTask.getSelfID());
                        intent.putExtra("longTaskName", longTask.getName());
                        startActivity(intent);
                    }
                    editLongTask = false;
                }
            }

            @Override
            public void onItemLongClick(RecyclerView.ViewHolder vh) {
                if (vh.getLayoutPosition() != 0 && vh.getLayoutPosition() != 1) {
                    mItemTouchHelper.startDrag(vh);
                }
            }
        });
        swipeRecyclerView.setAdapter(taskAdapter);
        displayName.setText(tasklistName);
        newTask = findViewById(R.id.add_new_task);
        newTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                View dialogView = View.inflate(mContext, R.layout.choose_task_type, null);
                final AlertDialog alertDialog = builder.create();
                alertDialog.setView(dialogView);
                final Button permanent = dialogView.findViewById(R.id.permanent_task);
                permanent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        final AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
                        builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder1.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                BaseTask baseTask = new BaseTask();
                                baseTask.setName(newTaskName);
                                baseTask.setStatus(BaseTask.UNFINISHED);
                                baseTask.setDeadline(newTaskDeadline);
                                baseTask.setDescription(newTaskDescription);
                                baseTask.setFatherID(taskListID);
                                baseTask.setSelfID(String.valueOf(System.currentTimeMillis()));
                                tasks.add(baseTask);
                                baseTask.saveThrows();
                                taskAdapter.notifyDataSetChanged();
                                newTaskDeadline = "Deadline unset";
                                newTaskName = "Unnamed";
                                newTaskDescription = "No description";
                                dialog.dismiss();
                            }
                        });
                        View permanentView = View.inflate(mContext, R.layout.create_permanent_task, null);
                        AlertDialog createPermanentTaskDialog = builder1.create();
                        createPermanentTaskDialog.setView(permanentView);
                        final EditText name = permanentView.findViewById(R.id.new_permanent_task_name);
                        final EditText description = permanentView.findViewById(R.id.new_permanent_task_description);
                        final TextView deadline = permanentView.findViewById(R.id.new_permanent_task_deadline);
                        final ImageView deadlineChoose = permanentView.findViewById(R.id.new_permanent_task_deadline_choose);
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
                        createPermanentTaskDialog.show();
                    }
                });

                final Button cycle = dialogView.findViewById(R.id.cycle_task);
                cycle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        final AlertDialog.Builder builder2 = new AlertDialog.Builder(mContext);
                        builder2.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder2.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CycleTask cycleTask = new CycleTask();
                                cycleTask.setCycleDays(Long.parseLong(newCycleTaskCycleDays));
                                cycleTask.setName(newTaskName);
                                cycleTask.setDescription(newTaskDescription);
                                cycleTask.setDeadline(newTaskDeadline);
                                cycleTask.setStatus(BaseTask.UNFINISHED);
                                cycleTask.setFatherID(taskListID);
                                cycleTask.setTimes(newCycleTaskTimes);
                                cycleTask.setSelfID(String.valueOf(System.currentTimeMillis()));
                                tasks.add(cycleTask);
                                cycleTask.save();
                                taskAdapter.notifyDataSetChanged();
                                newTaskDeadline = "Deadline unset";
                                newTaskName = "Unnamed";
                                newTaskDescription = "No description";
                                newCycleTaskCycleDays = "Unset";
                                newCycleTaskTimes = 1;
                                dialog.dismiss();
                            }
                        });
                        View cycleView = View.inflate(mContext, R.layout.create_cycle_task, null);
                        AlertDialog createCycleTaskDialog = builder2.create();
                        createCycleTaskDialog.setView(cycleView);
                        final EditText name = cycleView.findViewById(R.id.new_cycle_task_name);
                        final EditText description = cycleView.findViewById(R.id.new_cycle_task_description);
                        final TextView deadline = cycleView.findViewById(R.id.new_cycle_task_deadline);
                        final ImageView deadlineChoose = cycleView.findViewById(R.id.new_cycle_task_deadline_choose);
                        final EditText cycleDays = cycleView.findViewById(R.id.new_cycle_task_cycle);
                        final TextView nextDeadline = cycleView.findViewById(R.id.next_deadline);
                        final EditText times = cycleView.findViewById(R.id.new_cycle_task_times);
                        times.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                newCycleTaskTimes = Integer.parseInt(times.getText().toString());
                            }
                        });
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
                                        if (!(newTaskDeadline == null || newCycleTaskCycleDays.equals("") || newTaskDeadline.equals("Deadline unset"))) {
                                            Calendar calendar = Calendar.getInstance();
                                            String[] dates = deadline.getText().toString().split("-");
                                            int year2 = Integer.parseInt(dates[0]);
                                            int month = Integer.parseInt(dates[1]) - 1;
                                            int day = Integer.parseInt(dates[2]);
                                            calendar.set(Calendar.YEAR, year2);
                                            calendar.set(Calendar.MONTH, month);
                                            calendar.set(Calendar.DAY_OF_MONTH, day);
                                            calendar.add(Calendar.DATE, (int) Long.parseLong(newCycleTaskCycleDays));
                                            year = calendar.get(Calendar.YEAR);
                                            month = calendar.get(Calendar.MONTH);
                                            day = calendar.get(Calendar.DAY_OF_MONTH);
                                            String date2 = "Next Deadline is: " + year + "-" + (month + 1) + "-" + day;
                                            nextDeadline.setText(date2);
                                        } else {
                                            nextDeadline.setText("Unknown");
                                        }
                                    }
                                });
                                datePickerDialog.show();
                            }
                        });
                        createCycleTaskDialog.show();
                        cycleDays.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                newCycleTaskCycleDays = cycleDays.getText().toString();
                                if (!(newTaskDeadline == null || newCycleTaskCycleDays.equals("") || newTaskDeadline.equals("Deadline unset"))) {
                                    Calendar calendar = Calendar.getInstance();
                                    String[] dates = deadline.getText().toString().split("-");
                                    int year = Integer.parseInt(dates[0]);
                                    int month = Integer.parseInt(dates[1]) - 1;
                                    int day = Integer.parseInt(dates[2]);
                                    calendar.set(Calendar.YEAR, year);
                                    calendar.set(Calendar.MONTH, month);
                                    calendar.set(Calendar.DAY_OF_MONTH, day);
                                    calendar.add(Calendar.DATE, (int) Long.parseLong(newCycleTaskCycleDays));
                                    year = calendar.get(Calendar.YEAR);
                                    month = calendar.get(Calendar.MONTH);
                                    day = calendar.get(Calendar.DAY_OF_MONTH);
                                    String date = "Next Deadline is: " + year + "-" + (month + 1) + "-" + day;
                                    nextDeadline.setText(date);
                                } else {
                                    nextDeadline.setText("Unknown");
                                }
                            }
                        });
                    }
                });

                final Button longTask = dialogView.findViewById(R.id.long_task);
                longTask.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        final AlertDialog.Builder builder3 = new AlertDialog.Builder(mContext);
                        builder3.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder3.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                LongTask longTask = new LongTask();
                                longTask.setName(newTaskName);
                                longTask.setStatus(BaseTask.UNFINISHED);
                                longTask.setDeadline(newTaskDeadline);
                                longTask.setDescription(newTaskDescription);
                                longTask.setFatherID(taskListID);
                                longTask.setSelfID(String.valueOf(System.currentTimeMillis()));
                                tasks.add(longTask);
                                longTask.save();
                                taskAdapter.notifyDataSetChanged();
                                newTaskDeadline = "Deadline unset";
                                newTaskName = "Unnamed";
                                newTaskDescription = "No description";
                                dialog.dismiss();
                            }
                        });
                        View LongView = View.inflate(mContext, R.layout.create_long_task, null);
                        AlertDialog createLongTaskDialog = builder3.create();
                        createLongTaskDialog.setView(LongView);
                        final EditText name2 = LongView.findViewById(R.id.new_long_task_name);
                        final EditText description2 = LongView.findViewById(R.id.new_long_task_description);
                        final TextView deadline2 = LongView.findViewById(R.id.new_long_task_deadline);
                        final ImageView deadlineChoose2 = LongView.findViewById(R.id.new_long_task_deadline_choose);
                        name2.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                newTaskName = name2.getText().toString();
                            }
                        });
                        description2.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                newTaskDescription = description2.getText().toString();
                            }
                        });
                        deadlineChoose2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder datePickerBuilder = new AlertDialog.Builder(mContext);
                                datePickerBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        newTaskDeadline = deadline2.getText().toString();
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
                                if (deadline2.getText().toString().equals("Deadline unset")) {
                                    Calendar calendar = Calendar.getInstance();
                                    initYear = calendar.get(Calendar.YEAR);
                                    initMonth = calendar.get(Calendar.MONTH);
                                    initDay = calendar.get(Calendar.DAY_OF_MONTH);
                                } else {
                                    String[] dates = deadline2.getText().toString().split("-");
                                    initYear = Integer.parseInt(dates[0]);
                                    initMonth = Integer.parseInt(dates[1]) - 1;
                                    initDay = Integer.parseInt(dates[2]);
                                }
                                final DatePicker datePicker = datePickerView.findViewById(R.id.date_picker);
                                datePicker.init(initYear, initMonth, initDay, new DatePicker.OnDateChangedListener() {
                                    @Override
                                    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                        String date = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                                        deadline2.setText(date);
                                    }
                                });
                                datePickerDialog.show();
                            }
                        });
                        createLongTaskDialog.show();
                    }
                });
                alertDialog.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.activity_task_list_display, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.task_sort_by_name:
                Collections.sort(tasks, new Comparator<BaseTask>() {
                    @Override
                    public int compare(BaseTask o1, BaseTask o2) {
                        return (o1.getName()).compareTo(o2.getName());
                    }
                });
                taskAdapter.notifyDataSetChanged();
                break;
            case R.id.task_sort_by_deadline:
                Collections.sort(tasks, new Comparator<BaseTask>() {
                    @Override
                    public int compare(BaseTask o1, BaseTask o2) {
                        return (o1.getDeadline()).compareTo(o2.getDeadline());
                    }
                });
                taskAdapter.notifyDataSetChanged();
                break;

            case R.id.task_sort_by_status:
                Collections.sort(tasks, new Comparator<BaseTask>() {
                    @Override
                    public int compare(BaseTask o1, BaseTask o2) {
                        return o1.getStatus() - o2.getStatus();
                    }
                });
                taskAdapter.notifyDataSetChanged();
                break;
            case R.id.move_task:
                Toast.makeText(mContext, "Click a task to move", Toast.LENGTH_SHORT).show();
                copyMode = false;
                moveMode = true;
                editLongTask = false;
                break;
            case R.id.copy_task:
                Toast.makeText(mContext, "Click a task to copy", Toast.LENGTH_SHORT).show();
                copyMode = true;
                moveMode = false;
                editLongTask = false;
                break;
            case R.id.manage_long_task:
                Toast.makeText(mContext, "Click a longTask to manage its subtasks", Toast.LENGTH_SHORT).show();
                copyMode = false;
                moveMode = false;
                editLongTask = true;
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
