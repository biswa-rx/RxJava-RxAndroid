package com.example.rxjava_rxandroid;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "FlowableDemo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create a Flowable with backpressure strategy (BUFFER)
        Flowable<Integer> flowable = Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> emitter) throws Exception {
                for (int i = 1; i <= 10000; i++) {
                    // Emit items with delay to simulate a slow producer
                    emitter.onNext(i);
                    Thread.sleep(10);
                }
                emitter.onComplete();
            }
        }, BackpressureStrategy.BUFFER);

        // Subscriber listens to the Flowable on a background thread
        flowable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        item -> {
                            // Process the emitted items on the main thread
                            Log.d(TAG, "Received item: " + item);
                        },
                        throwable -> {
                            // Handle any errors
                            Log.e(TAG, "Error: " + throwable.getMessage());
                        },
                        () -> {
                            // Called when the Flowable completes
                            Log.d(TAG, "Flowable completed");
                        }
                );
    }
}

//BackpressureStrategy

//        BUFFER
//        Buffers all onNext values until the downstream consumes it.
//
//        DROP
//        Drops the most recent onNext value if the downstream can't keep up.
//
//        ERROR
//        Signals a MissingBackpressureException in case the downstream can't keep up.
//
//        LATEST
//        Keeps only the latest onNext value, overwriting any previous value if the downstream can't keep up.
//
//        MISSING
//        OnNext events are written without any buffering or dropping.