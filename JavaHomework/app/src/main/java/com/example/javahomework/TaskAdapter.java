package com.example.javahomework;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.javahomework.Tasks.BaseTask;
import com.example.javahomework.Tasks.CycleTask;
import com.example.javahomework.Tasks.LongTask;
import com.example.javahomework.Tasks.SubTask;

import org.litepal.LitePal;

import java.util.Calendar;
import java.util.List;


public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {
    private List<BaseTask> tasks;
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    private String taskName;
    private String taskDescription;
    private String taskDeadline;
    private String taskCycleDays;
    private int taskCycleTimes;

    public TaskAdapter(List<BaseTask> tasks, Context context) {
        this.tasks = tasks;
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public TaskAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(mLayoutInflater.inflate(R.layout.task_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.taskTitle.setText(tasks.get(position).getName());
        BaseTask task = tasks.get(position);
        if (task instanceof LongTask) {
            holder.taskTitle.setTextColor(Color.BLUE);
        } else if (task instanceof CycleTask) {
            holder.taskTitle.setTextColor(Color.GREEN);
        } else if (task instanceof SubTask) {
            holder.taskTitle.setTextColor(Color.CYAN);
        } else {
            holder.taskTitle.setTextColor(Color.RED);
        }
        holder.taskDeadline.setText(tasks.get(position).getDeadline());
        holder.taskFinished.setChecked(tasks.get(position).getStatus() == BaseTask.FINISHED);
        holder.taskFinished.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newStatus;
                if (tasks.get(position).getStatus() == BaseTask.FINISHED) {
                    newStatus = BaseTask.UNFINISHED;
                } else {
                    newStatus = BaseTask.FINISHED;
                }
                BaseTask task = tasks.get(position);
                task.setStatus(newStatus);
                String currStatus = task.getSelfID();
                ContentValues contentValues = new ContentValues();
                contentValues.put("status", newStatus);
                if (task instanceof LongTask) {
                    LitePal.updateAll(LongTask.class, contentValues, "selfID=?", currStatus);
                } else {
                    if (task instanceof CycleTask) {
                        LitePal.updateAll(CycleTask.class, contentValues, "selfID=?", currStatus);
                    } else if (task instanceof SubTask) {
                        LitePal.updateAll(SubTask.class, contentValues, "selfID=?", currStatus);
                    } else {
                        LitePal.updateAll(BaseTask.class, contentValues, "selfID=?", currStatus);
                    }
                }
            }
        });
        holder.taskDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BaseTask task = tasks.get(position);
                if (task instanceof LongTask) {
                    taskName = task.getName();
                    taskDeadline = task.getDeadline();
                    taskDescription = task.getDescription();
                    final AlertDialog.Builder base = new AlertDialog.Builder(mContext);
                    base.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    base.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            LongTask longTask = (LongTask) tasks.get(position);
                            longTask.setName(taskName);
                            longTask.setDeadline(taskDeadline);
                            longTask.setDescription(taskDescription);
                            ContentValues contentValues = new ContentValues();
                            contentValues.put("name", taskName);
                            LitePal.updateAll(LongTask.class, contentValues, "selfID=?", longTask.getSelfID());
                            contentValues = new ContentValues();
                            contentValues.put("deadline", taskDeadline);
                            LitePal.updateAll(LongTask.class, contentValues, "selfID=?", longTask.getSelfID());
                            contentValues = new ContentValues();
                            contentValues.put("description", taskDescription);
                            LitePal.updateAll(LongTask.class, contentValues, "selfID=?", longTask.getSelfID());
                            notifyDataSetChanged();
                        }
                    });
                    View baseView = View.inflate(mContext, R.layout.create_permanent_task, null);
                    AlertDialog baseDialog = base.create();
                    baseDialog.setView(baseView);
                    final EditText name = baseView.findViewById(R.id.new_permanent_task_name);
                    final EditText description = baseView.findViewById(R.id.new_permanent_task_description);
                    final TextView deadline = baseView.findViewById(R.id.new_permanent_task_deadline);
                    final ImageView deadlineChoose = baseView.findViewById(R.id.new_permanent_task_deadline_choose);
                    name.setText(task.getName());
                    description.setText(task.getDescription());
                    deadline.setText(task.getDeadline());
                    name.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            taskName = name.getText().toString();
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
                            taskDescription = description.getText().toString();
                        }
                    });
                    deadlineChoose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder datePickerBuilder = new AlertDialog.Builder(mContext);
                            datePickerBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    taskDeadline = deadline.getText().toString();
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
                    baseDialog.show();
                } else {
                    if (task instanceof CycleTask) {
                        taskCycleDays = String.valueOf(((CycleTask) task).getCycleDays());
                        taskName = task.getName();
                        taskCycleTimes = ((CycleTask) task).getTimes();
                        taskDeadline = task.getDeadline();
                        taskDescription = task.getDescription();
                        final AlertDialog.Builder cycle = new AlertDialog.Builder(mContext);
                        cycle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        cycle.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CycleTask cycleTask = (CycleTask) tasks.get(position);
                                cycleTask.setCycleDays(Long.parseLong(taskCycleDays));
                                cycleTask.setName(taskName);
                                cycleTask.setTimes(taskCycleTimes);
                                cycleTask.setDeadline(taskDeadline);
                                cycleTask.setDescription(taskDescription);
                                ContentValues contentValues = new ContentValues();
                                contentValues.put("name", taskName);
                                LitePal.updateAll(CycleTask.class, contentValues, "selfID=?", cycleTask.getSelfID());
                                contentValues = new ContentValues();
                                contentValues.put("deadline", taskDeadline);
                                LitePal.updateAll(CycleTask.class, contentValues, "selfID=?", cycleTask.getSelfID());
                                contentValues = new ContentValues();
                                contentValues.put("description", taskDescription);
                                LitePal.updateAll(CycleTask.class, contentValues, "selfID=?", cycleTask.getSelfID());
                                contentValues = new ContentValues();
                                contentValues.put("cycleDays", taskCycleDays);
                                LitePal.updateAll(CycleTask.class, contentValues, "selfID=?", cycleTask.getSelfID());
                                contentValues = new ContentValues();
                                contentValues.put("times", taskCycleTimes);
                                LitePal.updateAll(CycleTask.class, contentValues, "selfID=?", cycleTask.getSelfID());
                                notifyDataSetChanged();
                            }
                        });
                        final View cycleView = View.inflate(mContext, R.layout.create_cycle_task, null);
                        AlertDialog cycleTaskDialog = cycle.create();
                        cycleTaskDialog.setView(cycleView);
                        final EditText name = cycleView.findViewById(R.id.new_cycle_task_name);
                        final EditText description = cycleView.findViewById(R.id.new_cycle_task_description);
                        final TextView deadline = cycleView.findViewById(R.id.new_cycle_task_deadline);
                        final ImageView deadlineChoose = cycleView.findViewById(R.id.new_cycle_task_deadline_choose);
                        final EditText cycleDays = cycleView.findViewById(R.id.new_cycle_task_cycle);
                        final TextView nextDeadline = cycleView.findViewById(R.id.next_deadline);
                        final EditText cycleTimes = cycleView.findViewById(R.id.new_cycle_task_times);
                        name.setText(task.getName());
                        description.setText(task.getDescription());
                        deadline.setText(task.getDeadline());
                        cycleDays.setText(String.valueOf(((CycleTask) task).getCycleDays()));
                        cycleTimes.setText(String.valueOf(((CycleTask) task).getTimes()));
                        String[] dates = deadline.getText().toString().split("-");
                        int year2 = Integer.parseInt(dates[0]);
                        int month = Integer.parseInt(dates[1]) - 1;
                        int day = Integer.parseInt(dates[2]);
                        Calendar calendar2 = Calendar.getInstance();
                        calendar2.set(Calendar.YEAR, year2);
                        calendar2.set(Calendar.MONTH, month);
                        calendar2.set(Calendar.DAY_OF_MONTH, day);
                        calendar2.add(Calendar.DATE, (int) ((CycleTask) task).getCycleDays());
                        year2 = calendar2.get(Calendar.YEAR);
                        month = calendar2.get(Calendar.MONTH);
                        day = calendar2.get(Calendar.DAY_OF_MONTH);
                        String date2 = "Next Deadline is: " + year2 + "-" + (month + 1) + "-" + day;
                        nextDeadline.setText(date2);
                        name.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                taskName = name.getText().toString();
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
                                taskDescription = description.getText().toString();
                            }
                        });
                        deadlineChoose.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder datePickerBuilder = new AlertDialog.Builder(mContext);
                                datePickerBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        taskDeadline = deadline.getText().toString();
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
                                        if (!(taskDeadline == null || taskCycleDays.equals("") || taskDeadline.equals("Deadline unset"))) {
                                            Calendar calendar = Calendar.getInstance();
                                            String[] dates = deadline.getText().toString().split("-");
                                            int year2 = Integer.parseInt(dates[0]);
                                            int month = Integer.parseInt(dates[1]) - 1;
                                            int day = Integer.parseInt(dates[2]);
                                            calendar.set(Calendar.YEAR, year2);
                                            calendar.set(Calendar.MONTH, month);
                                            calendar.set(Calendar.DAY_OF_MONTH, day);
                                            calendar.add(Calendar.DATE, (int) Long.parseLong(taskCycleDays));
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
                        cycleTaskDialog.show();
                        cycleDays.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                taskCycleDays = cycleDays.getText().toString();
                                if (!(taskDeadline == null || taskCycleDays.equals("") || taskDeadline.equals("Deadline unset"))) {
                                    Calendar calendar = Calendar.getInstance();
                                    String[] dates = deadline.getText().toString().split("-");
                                    int year = Integer.parseInt(dates[0]);
                                    int month = Integer.parseInt(dates[1]) - 1;
                                    int day = Integer.parseInt(dates[2]);
                                    calendar.set(Calendar.YEAR, year);
                                    calendar.set(Calendar.MONTH, month);
                                    calendar.set(Calendar.DAY_OF_MONTH, day);
                                    calendar.add(Calendar.DATE, (int) Long.parseLong(taskCycleDays));
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
                        cycleTimes.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                taskCycleTimes = Integer.parseInt(cycleDays.getText().toString());
                            }
                        });
                    } else {
                        taskName = task.getName();
                        taskDeadline = task.getDeadline();
                        taskDescription = task.getDescription();
                        final AlertDialog.Builder base = new AlertDialog.Builder(mContext);
                        base.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        base.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                BaseTask baseTask = tasks.get(position);
                                baseTask.setName(taskName);
                                baseTask.setDeadline(taskDeadline);
                                baseTask.setDescription(taskDescription);
                                ContentValues contentValues = new ContentValues();
                                contentValues.put("name", taskName);
                                LitePal.updateAll(BaseTask.class, contentValues, "selfID=?", baseTask.getSelfID());
                                contentValues = new ContentValues();
                                contentValues.put("deadline", taskDeadline);
                                LitePal.updateAll(BaseTask.class, contentValues, "selfID=?", baseTask.getSelfID());
                                contentValues = new ContentValues();
                                contentValues.put("description", taskDescription);
                                LitePal.updateAll(BaseTask.class, contentValues, "selfID=?", baseTask.getSelfID());
                                notifyDataSetChanged();
                            }
                        });
                        View baseView = View.inflate(mContext, R.layout.create_permanent_task, null);
                        AlertDialog baseDialog = base.create();
                        baseDialog.setView(baseView);
                        final EditText name = baseView.findViewById(R.id.new_permanent_task_name);
                        final EditText description = baseView.findViewById(R.id.new_permanent_task_description);
                        final TextView deadline = baseView.findViewById(R.id.new_permanent_task_deadline);
                        final ImageView deadlineChoose = baseView.findViewById(R.id.new_permanent_task_deadline_choose);
                        name.setText(task.getName());
                        description.setText(task.getDescription());
                        deadline.setText(task.getDeadline());
                        name.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                taskName = name.getText().toString();
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
                                taskDescription = description.getText().toString();
                            }
                        });
                        deadlineChoose.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder datePickerBuilder = new AlertDialog.Builder(mContext);
                                datePickerBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        taskDeadline = deadline.getText().toString();
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
                        baseDialog.show();
                    }
                }
            }
        });
    }

    public int getItemCount() {
        return tasks == null ? 0 : tasks.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle;
        TextView taskDeadline;
        CheckBox taskFinished;
        LinearLayout taskItem, taskHidden, taskDisplay;

        public ViewHolder(View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.task_title);
            taskDeadline = itemView.findViewById(R.id.task_deadline);
            taskFinished = itemView.findViewById(R.id.task_finished);
            taskItem = itemView.findViewById(R.id.task_item);
            taskHidden = itemView.findViewById(R.id.task_item_hidden);
            taskDisplay = itemView.findViewById(R.id.task_item_display);
        }
    }
}
