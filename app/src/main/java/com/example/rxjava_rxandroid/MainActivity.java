package com.example.rxjava_rxandroid;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.example.rxjava_rxandroid.utils.DataSource;
import com.example.rxjava_rxandroid.utils.Task;
import com.jakewharton.rxbinding3.view.RxView;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Function;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Predicate;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.Unit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "biswa_rx";
    //ui
    TextView textView;
    private SearchView searchView;
    //vars
    private final CompositeDisposable disposables = new CompositeDisposable(); //For reactiveX rxjava3
    private final io.reactivex.disposables.CompositeDisposable disposable2 = new io.reactivex.disposables.CompositeDisposable(); //For reactiveX
    private long timeSinceLastRequest; // for log printouts only. Not part of logic.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        searchView = findViewById(R.id.search_bar);

        timeSinceLastRequest = System.currentTimeMillis();

//        initialLecture();

//        createOperatorSingleObject();

//        createOperatorListOfObject();

//        justOperator();

//        rangeOperator();

//        rangeOperatorUsingMap();

//        timerOperator();

//        bufferOperatorTrackingUIInteractions();

//        debounceOperatorRestrictServerRequests();

        throttleFirstOperatorRestrictButtonSpamming();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
        disposables.dispose();

        disposable2.dispose();
    }

    private void initialLecture(){
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
                Log.d(TAG, "onSubscribe: called");
                disposables.add(d);
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

        disposables.add(taskObservable.subscribe(new Consumer<Task>() {
            @Override
            public void accept(Task task) throws Throwable {

            }
        }));
    }

    private void bufferOperatorTrackingUIInteractions(){
        // detect clicks to a button
        RxView.clicks(findViewById(R.id.button))
                .map(new Function<Unit, Integer>() { // convert the detected clicks to an integer
                    @Override
                    public Integer apply(Unit unit) throws Exception {
                        return 1;
                    }
                })
                .buffer(4, TimeUnit.SECONDS) // capture all the clicks during a 4 second interval
                .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe(new io.reactivex.Observer<List<Integer>>() {
                    @Override
                    public void onSubscribe(io.reactivex.disposables.Disposable d) {
                        disposable2.add(d); // add to disposables to you can clear in onDestroy
                    }

                    @Override
                    public void onNext(List<Integer> integers) {
                        Log.d(TAG, "onNext: You clicked " + integers.size() + " times in 4 seconds!");
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });


    }

    private void debounceOperatorRestrictServerRequests(){
        // create the Observable
        Observable<String> observableQueryText = Observable
                .create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(final ObservableEmitter<String> emitter) throws Exception {

                        // Listen for text input into the SearchView
                        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                            @Override
                            public boolean onQueryTextSubmit(String query) {
                                return false;
                            }

                            @Override
                            public boolean onQueryTextChange(final String newText) {
                                if(!emitter.isDisposed()){
                                    emitter.onNext(newText); // Pass the query to the emitter
                                }
                                return false;
                            }
                        });
                    }
                })
                .debounce(500, TimeUnit.MILLISECONDS) // Apply Debounce() operator to limit requests
                .subscribeOn(Schedulers.io());

        // Subscribe an Observer
        observableQueryText.subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposables.add(d);
            }
            @Override
            public void onNext(String s) {
                Log.d(TAG, "onNext: time  since last request: " + (System.currentTimeMillis() - timeSinceLastRequest));
                Log.d(TAG, "onNext: search query: " + s);
                timeSinceLastRequest = System.currentTimeMillis();

                // method for sending a request to the server
                sendRequestToServer(s);
            }
            @Override
            public void onError(Throwable e) {
            }
            @Override
            public void onComplete() {
            }
        });
    }

    private void throttleFirstOperatorRestrictButtonSpamming() {
        // Set a click listener to the button with RxBinding Library
        RxView.clicks(findViewById(R.id.button))
                .throttleFirst(500, TimeUnit.MILLISECONDS) // Throttle the clicks so 500 ms must pass before registering a new click
                .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe(new io.reactivex.Observer<Unit>() {
                    @Override
                    public void onSubscribe(io.reactivex.disposables.Disposable d) {
                        disposable2.add(d);
                    }
                    @Override
                    public void onNext(Unit unit) {
                        Log.d(TAG, "onNext: time since last clicked: " + (System.currentTimeMillis() - timeSinceLastRequest));
                        timeSinceLastRequest = System.currentTimeMillis();
                        sendRequestToServer("Some Request"); // Execute some method when a click is registered
                    }
                    @Override
                    public void onError(Throwable e) {
                    }
                    @Override
                    public void onComplete() {
                    }
                });
    }

    // Fake method for sending a request to the server
    private void sendRequestToServer(String query){
        // do nothing
    }

}