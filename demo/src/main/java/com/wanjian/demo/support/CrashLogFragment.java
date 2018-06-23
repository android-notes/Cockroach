package com.wanjian.demo.support;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wanjian.demo.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class CrashLogFragment extends Fragment {

    RecyclerView recyclerView;
    Handler fileReadHandler;
    Handler uiHandler = new Handler();
    LogAdapter adapter = new LogAdapter();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_crash_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        DividerItemDecoration decoration = new DividerItemDecoration(getContext(), RecyclerView.VERTICAL);
        decoration.setDrawable(getResources().getDrawable(R.drawable.list_divider_horizontal));
        recyclerView.addItemDecoration(decoration);

        HandlerThread thread = new HandlerThread("crash_log_read") {
            @Override
            protected void onLooperPrepared() {
                super.onLooperPrepared();
                fileReadHandler = new Handler(getLooper());
                readFileList();
            }
        };
        thread.start();


    }

    private void readFileList() {
        fileReadHandler.post(new Runnable() {
            @Override
            public void run() {
                String dir = CrashLog.crashLogDir(getContext());
                if (dir == null) {
                    return;
                }
                File file = new File(dir);
                List<File> fs = Arrays.asList(file.listFiles());

                Collections.sort(fs, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        return (int) (o2.lastModified() - o1.lastModified());
                    }
                });

                final List<Log> logs = new ArrayList<>();
                for (File f : fs) {
                    logs.add(new Log(f, f.getName(), null));
                }
                setFileList(logs);
            }
        });
    }

    private void setFileList(final List<Log> logs) {

        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                adapter.setFileList(logs);
            }
        });
    }

    private void readFileContent(final File file) {

        fileReadHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    String content = read(file);
                    update(file, content);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void update(final File file, final String content) {
        if (content == null) {
            return;
        }

        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                for (Log log : adapter.logs) {
                    if (log.file == file) {
                        log.content = content;
                        adapter.notifyDataSetChanged();
                        return;
                    }

                }

            }
        });
    }

    private String read(File file) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line;

        StringBuilder builder = new StringBuilder((int) file.length());
        while ((line = reader.readLine()) != null) {
            builder.append(line);
            builder.append("\n");
        }
        return builder.toString();

    }

    class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogVH> {

        private List<Log> logs;

        @NonNull
        @Override
        public LogVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new LogVH();
        }

        @Override
        public void onBindViewHolder(@NonNull LogVH holder, int position) {
            holder.title.setTag(position);
            Log log = getData(position);
            holder.copy.setTag(log.content);
            holder.title.setText(log.title);
            if (log.content == null) {
                holder.content.setVisibility(View.GONE);
                holder.copy.setVisibility(View.INVISIBLE);
            } else {
                holder.content.setText(log.content);
                holder.content.setVisibility(View.VISIBLE);
                holder.copy.setVisibility(View.VISIBLE);
            }
        }

        protected Log getData(int position) {
            return logs.get(position);
        }

        @Override
        public int getItemCount() {
            return logs == null ? 0 : logs.size();
        }

        public void setFileList(List<Log> fs) {
            this.logs = fs;
            notifyDataSetChanged();
        }

        class LogVH extends RecyclerView.ViewHolder {

            TextView title;
            TextView content;
            TextView copy;

            public LogVH() {
                super(LayoutInflater.from(getContext()).inflate(R.layout.item_crash_log, recyclerView, false));
                title = itemView.findViewById(R.id.title);
                content = itemView.findViewById(R.id.content);
                copy = itemView.findViewById(R.id.copy);
                title.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = ((int) v.getTag());
                        if (logs.get(position).content == null) {
                            readFileContent(logs.get(position).file);
                        }
                    }
                });
                copy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String log = ((String) v.getTag());
                        ClipboardManager cmb = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        cmb.setText(log);
                        Toast.makeText(v.getContext(), "已经复制到粘贴板", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }


    }

    class Log {
        File file;
        String title;
        String content;

        public Log(File file, String title, String content) {
            this.file = file;
            this.title = title;
            this.content = content;
        }
    }
}

