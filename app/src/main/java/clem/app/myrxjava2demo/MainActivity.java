package clem.app.myrxjava2demo;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

//https://www.jianshu.com/p/b39afa92807e

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private StringBuilder mRxOperatorsText;
    private Disposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rxjava2Demos();
    }

    private void rxjava2Demos() {
        mRxOperatorsText = new StringBuilder();
//        rxDemo_create();
//        rxDemo_map();
//        rxDemo_zip();
//        rxDemo_concat();
//        rxDemo_flatmap();
//        rxDemo_concatmap();
//        rxDemo_distict();
//        rxDemo_filter();
//        rxDemo_buffer();
//        rxDemo_time();
        rxDemo_interval();
    }

    private void rxDemo_interval() {
        mRxOperatorsText.append("interval start : " + getNowStrTime() + "\n");
        Log.e(TAG, "interval start : " + getNowStrTime() + "\n");
        mDisposable = Observable.interval(5, 2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()) // interval 默认在新线程，所以需要切换回主线程
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        mRxOperatorsText.append("interval :" + aLong + " at " + getNowStrTime() + "\n");
                        Log.e(TAG, "interval :" + aLong + " at " + getNowStrTime() + "\n");
                    }
                });
    }

    //    相当于一个定时任务。在 1.x 中它还可以执行间隔逻辑，但在 2.x 中此功能被交给了 interval，下一个会介绍。
    //    但需要注意的是，timer 和 interval 均默认在新线程
    private void rxDemo_time() {
        mRxOperatorsText.append("timer start : " + getNowStrTime() + "\n");
        Log.e(TAG, "timer start : " + getNowStrTime() + "\n");
        Observable.timer(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()) // timer 默认在新线程，所以需要切换回主线程
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        mRxOperatorsText.append("timer :" + aLong + " at " + getNowStrTime() + "\n");
                        Log.e(TAG, "timer :" + aLong + " at " + getNowStrTime() + "\n");
                    }
                });
    }

    private String getNowStrTime() {
        long time = System.currentTimeMillis();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(time));
    }

    //    buffer 操作符接受两个参数，buffer(count,skip)，作用是将 Observable 中的数据按 skip (步长) 分成最大不超过 count 的 buffer ，
//    然后生成一个  Observable
    private void rxDemo_buffer() {
        Observable.just(1, 2, 3, 4, 5, 6)
                .buffer(3, 2)
                .subscribe(new Consumer<List<Integer>>() {
                    @Override
                    public void accept(List<Integer> integers) throws Exception {
                        mRxOperatorsText.append("buffer size : " + integers.size() + "\n");
                        Log.e(TAG, "buffer size : " + integers.size() + "\n");
                        mRxOperatorsText.append("buffer value : ");
                        Log.e(TAG, "buffer value : ");
                        for (Integer i : integers) {
                            mRxOperatorsText.append(i + "");
                            Log.e(TAG, i + "");
                        }
                        mRxOperatorsText.append("\n");
                        Log.e(TAG, "\n");
                    }
                });
    }

    //    接受一个参数，让其过滤掉不符合我们条件的值
    private void rxDemo_filter() {
        Observable.just(1, 20, 65, -5, 7, 19)
                .filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer integer) throws Exception {
                        return integer > 10;
                    }
                }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                mRxOperatorsText.append("filter : " + integer + "\n");
                Log.e(TAG, "filter : " + integer + "\n");
            }
        });
    }

    //    简单的去重
    private void rxDemo_distict() {
        Observable.just(1, 2, 3, 4, 12, 2)
                .distinct()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        mRxOperatorsText.append("distinct : " + integer + "\n");
                        Log.e(TAG, "distinct : " + integer + "\n");
                    }
                });
    }

    //    concatMap 与 FlatMap 的唯一区别就是 concatMap 保证了顺序，所以，我们就直接把 flatMap 替换为 concatMap 验证吧。
    private void rxDemo_concatmap() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
            }
        }).concatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Integer integer) throws Exception {
                List<String> list = new ArrayList<>();
                //重复三次
                for (int i = 0; i < 3; i++) {
                    list.add("I am value " + integer);
                }
                int delayTime = (int) (1 + Math.random() * 10);
                return Observable.fromIterable(list).delay(delayTime, TimeUnit.MILLISECONDS);
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Log.e(TAG, "flatMap : accept : " + s + "\n");
                        mRxOperatorsText.append("flatMap : accept : " + s + "\n");
                    }
                });
    }

    //    FlatMap 是一个很有趣的东西，我坚信你在实际开发中会经常用到。它可以把一个发射器 Observable 通过某种方法转换为多个 Observables，
//    然后再把这些分散的 Observables装进一个单一的发射器 Observable。
//    但有个需要注意的是，flatMap 并不能保证事件的顺序，如果需要保证，需要用到我们下面要讲的 ConcatMap。
    private void rxDemo_flatmap() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
            }
        }).flatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Integer integer) throws Exception {
                List<String> list = new ArrayList<>();
                //重复三次
                for (int i = 0; i < 3; i++) {
                    list.add("I am value " + integer);
                }
                int delayTime = (int) (1 + Math.random() * 10);
                return Observable.fromIterable(list).delay(delayTime, TimeUnit.MILLISECONDS);
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Log.e(TAG, "flatMap : accept : " + s + "\n");
                        mRxOperatorsText.append("flatMap : accept : " + s + "\n");
                    }
                });
    }

    //    发射器 B 把自己的三个孩子送给了发射器 A，让他们组合成了一个新的发射器，非常懂事的孩子，有条不紊的排序接收。
    private void rxDemo_concat() {
        Observable.concat(Observable.just(1, 2, 3), Observable.just(4, 5, 6))
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        mRxOperatorsText.append("concat : ").append(integer).append("\n");
                        Log.e(TAG, "concat : " + integer + "\n");
                    }
                });
    }

    //    专用于合并事件，该合并不是连接（连接操作符后面会说），而是两两配对，
//     也就意味着，最终配对出的 Observable 发射事件数目只和少的那个相同。
    private void rxDemo_zip() {
        Observable.zip(getStringObservable(), getIntegerObservable(), new BiFunction<String, Integer, String>() {
            @Override
            public String apply(String s, Integer integer) throws Exception {
                return s + integer;
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                mRxOperatorsText.append("zip : accept : ").append(s).append("\n");
                Log.e(TAG, "zip : accept : " + s + "\n");
            }
        });
    }

    private Observable<String> getStringObservable() {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                if (!e.isDisposed()) {
                    e.onNext("A");
                    mRxOperatorsText.append("String emit : A \n");
                    Log.e(TAG, "String emit : A \n");
                    e.onNext("B");
                    mRxOperatorsText.append("String emit : B \n");
                    Log.e(TAG, "String emit : B \n");
                    e.onNext("C");
                    mRxOperatorsText.append("String emit : C \n");
                    Log.e(TAG, "String emit : C \n");
                }
            }
        });
    }

    private Observable<Integer> getIntegerObservable() {
        return Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                if (!e.isDisposed()) {
                    e.onNext(1);
                    mRxOperatorsText.append("Integer emit : 1 \n");
                    Log.e(TAG, "Integer emit : 1 \n");
                    e.onNext(2);
                    mRxOperatorsText.append("Integer emit : 2 \n");
                    Log.e(TAG, "Integer emit : 2 \n");
                    e.onNext(3);
                    mRxOperatorsText.append("Integer emit : 3 \n");
                    Log.e(TAG, "Integer emit : 3 \n");
                    e.onNext(4);
                    mRxOperatorsText.append("Integer emit : 4 \n");
                    Log.e(TAG, "Integer emit : 4 \n");
                    e.onNext(5);
                    mRxOperatorsText.append("Integer emit : 5 \n");
                    Log.e(TAG, "Integer emit : 5 \n");
                }
            }
        });
    }

    //    将一个 Observable 通过某种函数关系，转换为另一种 Observable
    private void rxDemo_map() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(0);
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
            }
        }).map(new Function<Integer, String>() {
            @Override
            public String apply(Integer integer) throws Exception {
                return "This is result " + integer;
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                mRxOperatorsText.append("accept : ").append(s).append("\n");
                Log.e(TAG, "accept : " + s + "\n");
            }
        });
    }

    //    用于产生一个 Obserable 被观察者对象
    private void rxDemo_create() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                mRxOperatorsText.append("Observable emit 1" + "\n");
                Log.e(TAG, "Observable emit 1" + "\n");
                e.onNext(1);
                mRxOperatorsText.append("Observable emit 2" + "\n");
                Log.e(TAG, "Observable emit 2" + "\n");
                e.onNext(2);
                mRxOperatorsText.append("Observable emit 3" + "\n");
                Log.e(TAG, "Observable emit 3" + "\n");
                e.onNext(3);
                e.onComplete();
                mRxOperatorsText.append("Observable emit 4" + "\n");
                Log.e(TAG, "Observable emit 4" + "\n");
                e.onNext(4);
            }
        }).subscribe(new Observer<Integer>() {
            private int i;
            private Disposable mDisposable;

            @Override
            public void onSubscribe(Disposable d) {
                mRxOperatorsText.append("onSubscribe : ").append(d.isDisposed()).append("\n");
                Log.e(TAG, "onSubscribe : " + d.isDisposed() + "\n");
                mDisposable = d;
            }

            @Override
            public void onNext(Integer integer) {
                mRxOperatorsText.append("onNext : value : ").append(integer).append("\n");
                Log.e(TAG, "onNext : value : " + integer + "\n");
                i++;
                if (i == 3) {
                    // 在RxJava 2.x 中，新增的Disposable可以做到切断的操作，让Observer观察者不再接收上游事件
                    mDisposable.dispose();
                    mRxOperatorsText.append("onNext : isDisposable : ").append(mDisposable.isDisposed()).append("\n");
                    Log.e(TAG, "onNext : isDisposable : " + mDisposable.isDisposed() + "\n");
                }
            }

            @Override
            public void onError(Throwable e) {
                mRxOperatorsText.append("onError : value : ").append(e.getMessage()).append("\n");
                Log.e(TAG, "onError : value : " + e.getMessage() + "\n");
            }

            @Override
            public void onComplete() {
                mRxOperatorsText.append("onComplete" + "\n");
                Log.e(TAG, "onComplete" + "\n");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }
}
