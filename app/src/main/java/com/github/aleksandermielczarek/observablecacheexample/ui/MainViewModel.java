package com.github.aleksandermielczarek.observablecacheexample.ui;

import android.databinding.ObservableField;
import android.support.annotation.Nullable;

import com.github.aleksandermielczarek.observablecache.ObservableCache;
import com.github.aleksandermielczarek.observablecacheexample.service.ObservableService;

import org.parceler.Parcel;

import javax.inject.Inject;

import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Aleksander Mielczarek on 30.10.2016.
 */

public class MainViewModel {

    public static final String OBSERVABLE_CACHE_KEY_OBSERVABLE = "observable";
    public static final String OBSERVABLE_CACHE_KEY_SINGLE = "single";
    public static final String OBSERVABLE_CACHE_KEY_COMPLETABLE = "completable";
    public static final String OBSERVABLE_CACHE_KEY_OBSERVABLE_ERROR = "observableError";
    public static final String OBSERVABLE_CACHE_KEY_SINGLE_ERROR = "singleError";
    public static final String OBSERVABLE_CACHE_KEY_COMPLETABLE_ERROR = "completableError";

    public final ObservableField<String> result = new ObservableField<>();

    private final ObservableService observableService;
    private final ObservableCache observableCache;
    private final CompositeSubscription subscriptions = new CompositeSubscription();

    @Inject
    public MainViewModel(ObservableService observableService, ObservableCache observableCache) {
        this.observableService = observableService;
        this.observableCache = observableCache;
    }

    public void testObservable() {
        testObservable(observableService.observable()
                .compose(observableCache.cacheObservable(OBSERVABLE_CACHE_KEY_OBSERVABLE)));
    }

    public void testSingle() {
        testSingle(observableService.single()
                .compose(observableCache.cacheSingle(OBSERVABLE_CACHE_KEY_SINGLE)));
    }

    public void testCompletable() {
        testCompletable(observableService.completable()
                .compose(observableCache.cacheCompletable(OBSERVABLE_CACHE_KEY_COMPLETABLE)));
    }

    public void testObservableError() {
        testObservable(observableService.observableError()
                .compose(observableCache.cacheObservable(OBSERVABLE_CACHE_KEY_OBSERVABLE_ERROR)));
    }

    public void testSingleError() {
        testSingle(observableService.singleError()
                .compose(observableCache.cacheSingle(OBSERVABLE_CACHE_KEY_SINGLE_ERROR)));
    }

    public void testCompletableError() {
        testCompletable(observableService.completableError()
                .compose(observableCache.cacheCompletable(OBSERVABLE_CACHE_KEY_COMPLETABLE_ERROR)));
    }

    private void testObservable(Observable<String> testObservable) {
        subscriptions.add(testObservable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result::set, throwable -> result.set(throwable.getMessage())));
    }

    public void testSingle(Single<String> testSingle) {
        subscriptions.add(testSingle
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result::set, throwable -> result.set(throwable.getMessage())));
    }

    public void testCompletable(Completable completable) {
        subscriptions.add(completable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> result.set("completable"), throwable -> result.set(throwable.getMessage())));
    }

    public void restoreObservables() {
        observableCache.getObservable(OBSERVABLE_CACHE_KEY_OBSERVABLE, String.class)
                .ifPresent(this::testObservable)
                .thanGetSingle(OBSERVABLE_CACHE_KEY_SINGLE, String.class)
                .ifPresent(this::testSingle)
                .thanGetCompletable(OBSERVABLE_CACHE_KEY_COMPLETABLE)
                .ifPresent(this::testCompletable)
                .thanGetObservable(OBSERVABLE_CACHE_KEY_OBSERVABLE_ERROR, String.class)
                .ifPresent(this::testObservable)
                .thanGetSingle(OBSERVABLE_CACHE_KEY_SINGLE_ERROR, String.class)
                .ifPresent(this::testSingle)
                .thanGetCompletable(OBSERVABLE_CACHE_KEY_COMPLETABLE_ERROR)
                .ifPresent(this::testCompletable);
    }

    public void clear() {
        result.set("");
        unsubscribe();
        observableCache.clear();
    }

    public void unsubscribe() {
        subscriptions.clear();
    }

    public State saveState() {
        return new State(result.get());
    }

    public void restoreState(@Nullable State state) {
        if (state != null) {
            result.set(state.getResult());
        }
    }

    @Parcel
    public static class State {

        String result;

        public State() {

        }

        public State(String result) {
            this.result = result;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
    }

}