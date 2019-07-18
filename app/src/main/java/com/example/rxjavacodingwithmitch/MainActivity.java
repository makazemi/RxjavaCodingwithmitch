package com.example.rxjavacodingwithmitch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.os.Bundle;
import android.service.autofill.Dataset;
import android.util.Log;
import android.widget.TextView;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private static final String TAG = "MainActivity";
    private CompositeDisposable compositeDisposable=new CompositeDisposable();
    //ui
    private SearchView searchView;

    // vars
    private long timeSinceLastRequest; // for log printouts only. Not part of logic.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView=findViewById(R.id.text);

//        Observable<Task> taskObservable=Observable
////                .fromIterable(DataSource.createTasksList())
////                .subscribeOn(Schedulers.io())
////                .filter(new Predicate<Task>() {
////                    @Override
////                    public boolean test(Task task) throws Exception {
////                        Log.d(TAG, "test: "+Thread.currentThread().getName());
////                        try {
////                            Thread.sleep(1000);
////                        } catch (InterruptedException e) {
////                            e.printStackTrace();
////                        }
////
////                        return task.isComplete();
////                    }
////                })
////                .observeOn(AndroidSchedulers.mainThread());
////
////        taskObservable.subscribe(new Observer<Task>() {
////            @Override
////            public void onSubscribe(Disposable d) {
////                Log.d(TAG, "onSubscribe: called");
////                compositeDisposable.add(d);
////            }
////
////            @Override
////            public void onNext(Task task) {
////
////                Log.d(TAG, "onNext: "+Thread.currentThread().getName());
////                Log.d(TAG, "onNext: "+task.getDescription());
////
////            }
////
////            @Override
////            public void onError(Throwable e) {
////                Log.e(TAG, "onError: ",e );
////            }
////
////            @Override
////            public void onComplete() {
////                Log.d(TAG, "onComplete: called");
////            }
////        });


        searchView = findViewById(R.id.search_view);

        timeSinceLastRequest = System.currentTimeMillis();

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
                .debounce(2000, TimeUnit.MILLISECONDS) // Apply Debounce() operator to limit requests
                .subscribeOn(Schedulers.io());

        // Subscribe an Observer
        observableQueryText.subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                compositeDisposable.add(d);
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

    // Fake method for sending a request to the server
    private void sendRequestToServer(String query){
        // do nothing
    }

    // If you use compositeDisposable in the viewModel
    // you should clear it on the onClear method
    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }

    private void doCreateOperatorForSingleObject(){
        // Instantiate the object to become an Observable
        final Task task = new Task("Walk the dog", false, 4);

// Create the Observable
        Observable<Task> singleTaskObservable = Observable
                .create(new ObservableOnSubscribe<Task>() {
                    @Override
                    public void subscribe(ObservableEmitter<Task> emitter) throws Exception {
                        if(!emitter.isDisposed()){
                            emitter.onNext(task);
                            emitter.onComplete();
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

// Subscribe to the Observable and get the emitted object
        singleTaskObservable.subscribe(new Observer<Task>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Task task) {
                Log.d(TAG, "onNext: single task: " + task.getDescription());
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void doCreateOperatorForListObject(){
        // Create the Observable
        Observable<Task> taskListObservable = Observable
                .create(new ObservableOnSubscribe<Task>() {
                    @Override
                    public void subscribe(ObservableEmitter<Task> emitter) throws Exception {

                        // Inside the subscribe method iterate through the list of tasks and call onNext(task)
                        for(Task task: DataSource.createTasksList()){
                            if(!emitter.isDisposed()){
                                emitter.onNext(task);
                            }
                        }
                        // Once the loop is complete, call the onComplete() method
                        if(!emitter.isDisposed()){
                            emitter.onComplete();
                        }

                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

// Subscribe to the Observable and get the emitted objects
        taskListObservable.subscribe(new Observer<Task>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Task task) {
                Log.d(TAG, "onNext: task list: " + task.getDescription());
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void doJustOperator(){
        Observable.just("first", "second", "third", "fourth", "fifth", "sixth",
                "seventh", "eighth", "ninth", "tenth")
                .subscribeOn(Schedulers.io()) // What thread to do the work on
                .observeOn(AndroidSchedulers.mainThread()) // What thread to observe the results on
                .subscribe(new Observer<String>() { // view the results by creating a new observer
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "onSubscribe: called");
                    }

                    @Override
                    public void onNext(String s) {
                        Log.d(TAG, "onNext: " + s);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: done...");
                    }
                });
    }

    private void doRangeOperator(){
        Observable
                .range(0,11)
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        Log.d(TAG, "onNext: " + integer);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void doRepeatOperator(){
        Observable.range(0,3)
                .repeat(2)
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        Log.d(TAG, "onNext: " + integer);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void doMapAndTakeWhileOperator(){
        Observable<Task> observable=Observable
                .range(0,9)
                .subscribeOn(Schedulers.io())
                .map(new Function<Integer, Task>() {
                    @Override
                    public Task apply(Integer integer) throws Exception {
                        Log.d(TAG, "apply: "+Thread.currentThread().getName());
                        return new Task("this is a task with priority: "+String.valueOf(integer),
                                false,integer);
                    }
                })
                .takeWhile(new Predicate<Task>() {
                    @Override
                    public boolean test(Task task) throws Exception {
                        return task.getPriority()<9;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());

        observable.subscribe(new Observer<Task>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Task task) {
                Log.d(TAG, "onNext: "+task.getPriority());
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void doIntervalOperator(){
        // emit an observable every time interval
        Observable<Long> intervalObservable = Observable
                .interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .takeWhile(new Predicate<Long>() { // stop the process if more than 5 seconds passes
                    @Override
                    public boolean test(Long aLong) throws Exception {
                        return aLong <= 5;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());

        intervalObservable.subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
            }
            @Override
            public void onNext(Long aLong) {
                Log.d(TAG, "onNext: interval: " + aLong);
            }
            @Override
            public void onError(Throwable e) {

            }
            @Override
            public void onComplete() {

            }
        });
    }

    private void doTimerOperator(){
        // emit single observable after a given delay
        Observable<Long> timeObservable = Observable
                .timer(3, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        timeObservable.subscribe(new Observer<Long>() {

            long time = 0; // variable for demonstating how much time has passed

            @Override
            public void onSubscribe(Disposable d) {
                time = System.currentTimeMillis() / 1000;
            }
            @Override
            public void onNext(Long aLong) {
                Log.d(TAG, "onNext: " + ((System.currentTimeMillis() / 1000) - time) + " seconds have elapsed." );
            }
            @Override
            public void onError(Throwable e) {

            }
            @Override
            public void onComplete() {

            }
        });
    }

    private void doFromArrayOperator(){
        Task[] list = new Task[5];
        list[0] = (new Task("Take out the trash", true, 3));
        list[1] = (new Task("Walk the dog", false, 2));
        list[2] = (new Task("Make my bed", true, 1));
        list[3] = (new Task("Unload the dishwasher", false, 0));
        list[4] = (new Task("Make dinner", true, 5));

        Observable<Task> taskObservable = Observable
               // .fromArray(list)
                .fromIterable(DataSource.createTasksList())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        taskObservable.subscribe(new Observer<Task>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Task task) {
                Log.d(TAG, "onNext: : " + task.getDescription());
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void doFromCallbackOperator(){
        // create Observable (method will not execute yet)
       /* Observable<Task> callable = Observable
                .fromCallable(new Callable<Task>() {
                    @Override
                    public Task call() throws Exception {
                        //return MyDatabase.getTask();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

// method will be executed since now something has subscribed
        callable.subscribe(new Observer<Task>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Task task) {
                Log.d(TAG, "onNext: : " + task.getDescription());
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
        */
    }


}
