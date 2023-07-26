package com.example.rxjava_rxandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;

import com.example.rxjava_rxandroid.utils.DataSource;
import com.example.rxjava_rxandroid.utils.Task;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Predicate;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "biswa_rx";
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);


        Observable<Task> taskObservable = Observable // create a new Observable object
                .fromIterable(DataSource.createTasksList()) // apply 'fromIterable' operator
                .subscribeOn(Schedulers.io()) // designate worker thread (background)
                .filter(new Predicate<Task>() {
                    @Override
                    public boolean test(Task task) throws Throwable {
                        Log.d(TAG, "Subscriber Thread: "+Thread.currentThread().getName());
                        SystemClock.sleep(1000);
                        return task.isComplete();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread()); // designate observer thread (main thread)


        taskObservable.subscribe(new Observer<Task>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "onSubscribe: ");
            }
            @Override
            public void onNext(Task task) { // run on main thread

                Log.d(TAG, "onNext: : " + task.getDescription()+"  Thread - "+Thread.currentThread().getName());
            }
            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: ", e);
            }
            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete: ");
            }
        });

    }
}