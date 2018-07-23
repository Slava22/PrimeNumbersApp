package andrushkva.com.primenumbers;

import android.arch.persistence.room.Room;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import andrushkva.com.primenumbers.db.PrimeNumbers;
import andrushkva.com.primenumbers.db.PrimeNumbersDao;
import andrushkva.com.primenumbers.db.PrimeNumbersDatabase;
import rx.Observer;

public class MainActivity extends AppCompatActivity {

    private static final Integer COLUMNS_PORTRAIT = 2;
    private static final Integer COLUMNS_LANDSCAPE = 3;
    private static final int LIMIT = 50;

    private static final String EXTRA_isResetInfo = "is_reset_info";
    private static final String EXTRA_listPrimeNumbers = "list_prime_numbers";
    private static final String EXTRA_itemCount = "item_count";
    private static final String EXTRA_rvPosition = "rv_position";
    ArrayList<Integer> listPrimeNumbers;
    private boolean isResetInfo = false;
    private PrimeNumbersDatabase primeNumbersDatabase;
    private PrimeNumbersDao primeNumbersDao;

    private ProgressBar progressBar;
    private EditText etInterval;
    private EditText etThreads;
    private Button btnStart;
    private CheckBox cbWithThreads;
    private RecyclerView mRecyclerView;
    private PrimeNumbersAdapter mAdapter;
    private GridLayoutManager mLayoutManager;
    private LinearLayout emptyPrimeNumbersLayout;
    private ScrollView svHistory;
    private LinearLayout llHistory;

    private RecyclerView.OnScrollListener recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = mLayoutManager.getChildCount();
            int totalItemCount = mLayoutManager.getItemCount();
            int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();

            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0) {
                loadMoreItems(totalItemCount);
                notifyAdapterData();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progress_bar);
        mRecyclerView = findViewById(R.id.rv_prime_numbers);
        etInterval = findViewById(R.id.et_interval);
        etThreads = findViewById(R.id.et_threads);
        btnStart = findViewById(R.id.btn_start);
        cbWithThreads = findViewById(R.id.cb_threads);
        emptyPrimeNumbersLayout = findViewById(R.id.empty_prime_numbers_layout);
        svHistory = findViewById(R.id.sv_history);
        llHistory = findViewById(R.id.history);

        showProgressBar(false);

        listPrimeNumbers = new ArrayList<>();
        primeNumbersDatabase = Room.databaseBuilder(getApplicationContext(), PrimeNumbersDatabase.class,
                "primeNumbersDatabase").allowMainThreadQueries().build();
        primeNumbersDao = primeNumbersDatabase.primeNumbersDao();

        RxTextView.textChanges(etInterval)
                .debounce(500, TimeUnit.MILLISECONDS)
                .map(CharSequence::toString)
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(String s) {
                        if (!isResetInfo) {
                            MainActivity.this.runOnUiThread(() -> findPrimeNumbers(cbWithThreads.isChecked()));
                        }
                        isResetInfo = false;
                    }
                });

        btnStart.setOnClickListener(v -> {
                    findPrimeNumbers(cbWithThreads.isChecked());
                }
        );

        getScreenOrientation();
        mAdapter = new PrimeNumbersAdapter(getApplicationContext());
        RVEmptyObserver observer = new RVEmptyObserver(mRecyclerView, emptyPrimeNumbersLayout);
        mAdapter.registerAdapterDataObserver(observer);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(recyclerViewOnScrollListener);
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_isResetInfo, true);
        outState.putIntegerArrayList(EXTRA_listPrimeNumbers, listPrimeNumbers);
        outState.putInt(EXTRA_itemCount, mLayoutManager.getItemCount());
        outState.putInt(EXTRA_rvPosition, mLayoutManager.findFirstVisibleItemPosition());
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isResetInfo = savedInstanceState.getBoolean(EXTRA_isResetInfo);
        listPrimeNumbers = savedInstanceState.getIntegerArrayList(EXTRA_listPrimeNumbers);
        mAdapter.addNumbers(listPrimeNumbers.subList(0, savedInstanceState.getInt(EXTRA_itemCount)));
        notifyAdapterData();
        mRecyclerView.scrollToPosition(savedInstanceState.getInt(EXTRA_rvPosition));
    }

    private void getScreenOrientation() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mLayoutManager = new GridLayoutManager(getApplicationContext(), COLUMNS_PORTRAIT);
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mLayoutManager = new GridLayoutManager(getApplicationContext(), COLUMNS_LANDSCAPE);
        }
    }

    public void loadMoreItems(int totalItemCount) {
        mAdapter.addNumbers(listPrimeNumbers.subList(totalItemCount, Math.min(totalItemCount + LIMIT,
                listPrimeNumbers.size())));
    }

    public void notifyAdapterData() {
        mRecyclerView.post(() -> mAdapter.notifyDataSetChanged());
    }

    public void findPrimeNumbers(boolean withThreads) {
        showProgressBar(true);
        String intervalString = etInterval.getText().toString();
        if (!intervalString.equals("") && Integer.valueOf(intervalString) > 1) {
            String gsonStringPrimeNumbersDB = primeNumbersDao.getByInterval(Integer.valueOf(intervalString));
            if (gsonStringPrimeNumbersDB == null) {

                if (withThreads) {
                    findPrimeNumbersInThreads(intervalString);
                } else {
                    FindPrimeNumberTask myTask = new FindPrimeNumberTask();
                    myTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            } else {
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                listPrimeNumbers = gson.fromJson(gsonStringPrimeNumbersDB, new TypeToken<List<Integer>>() {
                }.getType());
                addPrimeNumbersInEmptyListAdapter();
            }
            addToHistory(intervalString);
        } else {
            mAdapter.clearNumbers();
            notifyAdapterData();
        }
        addToHistory(intervalString);
        showProgressBar(false);
    }

    public void findPrimeNumbersInThreads(String intervalString) {
        String threadsCount = etThreads.getText().toString();

        if (!threadsCount.equals("")) {

            if (Integer.valueOf(threadsCount) <= Integer.valueOf(intervalString)) {
                listPrimeNumbers = FindPrimeNumbersMultithreading.parallelFind(Integer.valueOf(intervalString), Integer.valueOf(threadsCount));
                insertListInDatabase(Integer.valueOf(etInterval.getText().toString()), listPrimeNumbers);
                addPrimeNumbersInEmptyListAdapter();
            } else {
                Toast.makeText(getApplicationContext(),
                        "The number of threads can not be more than the interval",
                        Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(getApplicationContext(), "Enter the number of threads",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void addToHistory(String interval) {
        TextView textView = new TextView(getApplicationContext());
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        textView.setText(interval);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(0, 2, 0, 2);
        textView.setTextSize(20f);
        textView.setOnClickListener(v -> {
            cbWithThreads.setChecked(false);
            etThreads.setText("");
            etInterval.setText(textView.getText());
        });
        llHistory.addView(textView);
        svHistory.post(() -> svHistory.fullScroll(ScrollView.FOCUS_DOWN));
    }

    public void showProgressBar(boolean show) {
        if (show) {
            MainActivity.this.runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));
        } else {
            MainActivity.this.runOnUiThread(() -> progressBar.setVisibility(View.GONE));
        }
    }

    public void addPrimeNumbersInEmptyListAdapter() {
        if (listPrimeNumbers.size() >= LIMIT) {
            mAdapter.changeList(listPrimeNumbers.subList(0, LIMIT));
        } else {
            mAdapter.changeList(listPrimeNumbers);
        }
        notifyAdapterData();
    }

    public void insertListInDatabase(int interval, List<Integer> list) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        primeNumbersDao.insert(new PrimeNumbers(interval, gson.toJson(list)));
    }

    class FindPrimeNumberTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            int[] arr = new int[Integer.valueOf(etInterval.getText().toString()) + 1];
            arr[0] = 0;
            arr[1] = 0;
            for (int i = 2; i < arr.length; i++) {
                arr[i] = 1;
            }

            for (int k = 2; k * k < arr.length; k++) {
                if (arr[k] == 1) {
                    for (int l = k * k; l < arr.length; l += k) {
                        arr[l] = 0;
                    }
                }
            }

            listPrimeNumbers.clear();
            for (int i = 2; i < arr.length; i++) {
                if (arr[i] == 1) {
                    listPrimeNumbers.add(i);
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            insertListInDatabase(Integer.valueOf(etInterval.getText().toString()), listPrimeNumbers);
            addPrimeNumbersInEmptyListAdapter();
            progressBar.setVisibility(View.GONE);
        }
    }
}