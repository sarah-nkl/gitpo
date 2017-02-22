/*
 * Copyright (c) 2016 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.example.githubrepo;

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.example.githubrepo.models.Repository;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;

/**
 * Created by sarahneo on 20/2/17.
 */

public class MainActivity extends BaseSearchActivity {

    private Disposable mDisposable;
    private int pageNum = 1; // Page number starts from 1

    @Override
    protected void onStart() {
        super.onStart();

        getRepoList();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    private void getRepoList() {
        Observable<String> buttonClickStream = createButtonClickObservable();
        Observable<String> textChangeStream = createTextChangeObservable();

        Observable<String> searchTextObservable = Observable.merge(textChangeStream, buttonClickStream);

        mDisposable = searchTextObservable
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        showProgressBar();
                    }
                })
                .observeOn(Schedulers.io())
                .map(new Function<String, List<Repository>>() {
                    @Override
                    public List<Repository> apply(String query) {
                        return mRepoSearchEngine.search(query, pageNum);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Repository>>() {
                    @Override
                    public void accept(List<Repository> result) {
                        hideProgressBar();
                        showResult(result);
                    }
                });

    }

    private Observable<String> createButtonClickObservable() {

        return Observable.create(new ObservableOnSubscribe<String>() {

            @Override
            public void subscribe(final ObservableEmitter<String> emitter) throws Exception {
                etQuery.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                            // Your piece of code on keyboard search click
                            emitter.onNext(etQuery.getText().toString());
                            return true;
                        }
                        return false;
                    }
                });

                emitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        //btnSearch.setOnClickListener(null);
                        etQuery.setOnEditorActionListener(null);
                    }
                });
            }
        });
    }

    private Observable<String> createTextChangeObservable() {
        Observable<String> textChangeObservable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> emitter) throws Exception {
                final TextWatcher watcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        etQuery.setCompoundDrawables(
                                null,
                                null,
                                etQuery.getText().toString().equals("") ? null : x, null);
                        emitter.onNext(s.toString());
                    }
                };

                etQuery.addTextChangedListener(watcher);

                emitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        etQuery.removeTextChangedListener(watcher);
                    }
                });
            }
        });

        return textChangeObservable
                .filter(new Predicate<String>() {
                    @Override
                    public boolean test(String query) throws Exception {
                        return query.length() >= 2;
                    }
                }).debounce(1000, TimeUnit.MILLISECONDS);  // Wait for x seconds before calling
    }

}
