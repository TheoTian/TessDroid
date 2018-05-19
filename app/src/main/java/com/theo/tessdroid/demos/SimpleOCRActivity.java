package com.theo.tessdroid.demos;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.theo.ocr.SimpleOCREngine;
import com.theo.ocr.traindata.IDownloader;
import com.theo.ocr.traindata.TrainDataDownloader;

import java.io.File;

public class SimpleOCRActivity extends AppCompatActivity {

    public static class TrainDataSource {

        private String name;
        private String url;
        private String language;

        private boolean downloaded;

        public TrainDataSource(String name, String url, String language) {
            this.name = name;
            this.url = url;
            this.language = language;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public String getLanguage() {
            return language;
        }

        public boolean isDownloaded() {
            return downloaded;
        }

        public void setDownloaded(boolean downloaded) {
            this.downloaded = downloaded;
        }
    }

    //train source
    private final static TrainDataSource[] sTrainSourceList = new TrainDataSource[]{
            new TrainDataSource("eng.traineddata", "https://raw.githubusercontent.com/tesseract-ocr/tessdata/master/eng.traineddata", "eng"),
            new TrainDataSource("chi_sim.traineddata", "https://raw.githubusercontent.com/tesseract-ocr/tessdata/master/chi_sim.traineddata", "chi_sim")
    };

    private Handler mHandler = new Handler() {

    };

    private RecyclerView trainDataRecycleView;
    private Button btnTrain;
    private Button btnProcess;
    private TextView tvResult;

    private String trainDataDir;

    private SimpleOCREngine simpleOCREngine;

    private String language = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_ocr);
        initData();
        initView();
    }

    private void initOCREngine() {
        simpleOCREngine = new SimpleOCREngine();
        simpleOCREngine.setOnInitListener(new SimpleOCREngine.OnInitListener() {
            @Override
            public void onIniting() {

            }

            @Override
            public void onInited() {

            }
        });

        simpleOCREngine.setOnProcessListener(new SimpleOCREngine.OnProcessListener() {
            @Override
            public void onProcessing() {

            }

            @Override
            public void onProcessed() {

            }
        });

        simpleOCREngine.setOnErrorListener(new SimpleOCREngine.OnErrorListener() {
            @Override
            public void onError() {

            }
        });
    }

    private void initData() {
        initOCREngine();

        trainDataDir = this.getExternalCacheDir() + "/tessdata/";

        File trainDataDirFile = new File(trainDataDir);
        if (trainDataDirFile.exists() && !trainDataDirFile.isDirectory()) {
            trainDataDirFile.delete();
        }

        if (!trainDataDirFile.exists()) {
            trainDataDirFile.mkdirs();
        }
        //check source
        for (TrainDataSource source : sTrainSourceList) {
            if (new File(trainDataDir + source.getName()).exists()) {
                updateInfoTrainDataComplete(source);
            }
        }
    }

    private void updateInfoTrainDataComplete(TrainDataSource source) {
        //add init language like eng+chi
        String prefix = "";
        if (!language.trim().equals("")) {
            prefix = "+";
        }
        language += prefix + source.getLanguage();
        source.setDownloaded(true);
    }

    private void initView() {
        trainDataRecycleView = findViewById(R.id.rvTrainDataList);
        // use a linear layout manager
        trainDataRecycleView.setLayoutManager(new LinearLayoutManager(this));
        trainDataRecycleView.setHasFixedSize(true);
        trainDataRecycleView.setAdapter(new RecyclerView.Adapter() {
            class CusViewHolder extends RecyclerView.ViewHolder {

                private ViewGroup viewGroup;

                public CusViewHolder(ViewGroup itemView) {
                    super(itemView);
                    viewGroup = itemView;
                }

                public ViewGroup getViewGroup() {
                    return viewGroup;
                }
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                ViewGroup itemView = (ViewGroup) LayoutInflater.from(SimpleOCRActivity.this)
                        .inflate(R.layout.layout_trainsource_item, null, false);
                return new CusViewHolder(itemView);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                final ViewGroup viewGroup = ((CusViewHolder) holder).getViewGroup();
                if (viewGroup == null) {
                    return;
                }
                if (position < 0 || position > sTrainSourceList.length - 1) {
                    return;
                }

                final TrainDataSource trainDataSource = sTrainSourceList[position];
                final TextView tvName = ((TextView) viewGroup.findViewById(R.id.tvName));
                tvName.setText(trainDataSource.name);

                final ProgressBar progressBar = (ProgressBar) viewGroup.findViewById(R.id.pbProgress);
                final Button btnDownload = (Button) viewGroup.findViewById(R.id.btnDownload);

                btnDownload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (trainDataSource.isDownloaded()) {
                            btnDownload.setText("Complete");
                            return;
                        }
                        download(trainDataSource, progressBar);
                    }
                });
                if (trainDataSource.isDownloaded()) {
                    progressBar.setProgress(progressBar.getMax());
                    btnDownload.setText("Complete");
                }
            }

            private void download(final TrainDataSource trainDataSource, final ProgressBar progressBar) {
                IDownloader downloader = new TrainDataDownloader();
                downloader.setResultCallback(new IDownloader.ResultCallback() {
                    @Override
                    public void onProgress(final int percent) {
                        progressBar.setProgress(percent);
                    }

                    @Override
                    public void onSuccess() {

                        updateInfoTrainDataComplete(trainDataSource);

                        Toast.makeText(SimpleOCRActivity.this, "Download Success", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed() {
                        Toast.makeText(SimpleOCRActivity.this, "Download Failed", Toast.LENGTH_SHORT).show();
                    }
                });
                downloader.download(trainDataSource.getUrl(), trainDataDir);
            }

            @Override
            public int getItemCount() {
                return sTrainSourceList.length;
            }


        });

        btnTrain = findViewById(R.id.btnTrain);
        btnTrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                train();
            }
        });

        btnProcess = findViewById(R.id.btnProcess);
        btnProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                process();
            }
        });

        tvResult = findViewById(R.id.tvResultShow);
    }

    /**
     * train the engine
     */
    private void train() {
        boolean result = simpleOCREngine.init(new File(trainDataDir).getParentFile().getAbsolutePath(), language);

        String msg;
        if (result) {
            msg = "Train Success";
        } else {
            msg = "Train Failure";
        }

        Toast.makeText(SimpleOCRActivity.this, msg, Toast.LENGTH_SHORT).show();

    }

    private void process() {
        if (!simpleOCREngine.isInited()) {
            Toast.makeText(SimpleOCRActivity.this, "Please Train First", Toast.LENGTH_SHORT).show();
            return;
        }
        simpleOCREngine.process(BitmapFactory.decodeResource(this.getResources(), R.drawable.test_1))

        if () {
            tvResult.setText(simpleOCREngine.getResult());
        } else {
            tvResult.setText("Process Failure");
        }

    }
}
