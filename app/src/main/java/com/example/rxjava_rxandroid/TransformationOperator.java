package com.example.rxjava_rxandroid;

import android.util.Log;

import com.example.rxjava_rxandroid.utils.DataSource;
import com.example.rxjava_rxandroid.utils.Task;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TransformationOperator {
    private static final String TAG = "TransformationOperator";

    public static void mapOperator(){

//        //Use this for mapping task to string
//        Function<Task, String> extractDescriptionFunction = new Function<Task, String>() {
//            @Override
//            public String apply(Task task) throws Exception {
//                Log.d(TAG, "apply: doing work on thread: " + Thread.currentThread().getName());
//                return task.getDescription();
//            }
//        };
//
//        //Use this for update same Task
//        Function<Task, Task> completeTaskFunction = new Function<Task, Task>() {
//            @Override
//            public Task apply(Task task) throws Exception {
//                Log.d(TAG, "apply: doing work on thread: " + Thread.currentThread().getName());
//                task.setComplete(true);
//                return task;
//            }
//        };

        Observable<String> extractDescriptionObservable = Observable
                .fromIterable(DataSource.createTasksList())
                .subscribeOn(Schedulers.io())
//                .map(extractDescriptionFunction) //You can do this way or
                .map(new Function<Task, String>() {
                    @Override
                    public String apply(Task task) throws Throwable {
                        Log.d(TAG, "apply: doing work on thread: " + Thread.currentThread().getName());
                        return task.getDescription();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());

        extractDescriptionObservable.subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
            }
            @Override
            public void onNext(String s) {
                Log.d(TAG, "onNext: extracted description: " + s);
            }
            @Override
            public void onError(Throwable e) {
            }
            @Override
            public void onComplete() {
            }
        });
    }


}
