package clem.app.myrxjava2demo;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

//https://www.jianshu.com/p/b39afa92807e
//https://www.jianshu.com/p/81fac37430dd

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
//        rxDemo_flatMap();
//        rxDemo_concatMap();
//        rxDemo_distinct();
//        rxDemo_filter();
//        rxDemo_buffer();
//        rxDemo_time();
//        rxDemo_interval();
//        rxDemo_doOnNext();
//        rxDemo_skip();
//        rxDemo_take();
//        rxDemo_just();
//        rxDemo_single();
//        rxDemo_debounce();
//        rxDemo_defer();
//        rxDemo_last();
//        rxDemo_merge();
//        rxDemo_reduce();
//        rxDemo_scan();
        rxDemo_window();
    }

    //    按照实际划分窗口，将数据发送给不同的 Observable
    private void rxDemo_window() {
        mRxOperatorsText.append("window\n");
        Log.e(TAG, "window\n");
        Observable.interval(1, TimeUnit.SECONDS)
                .take(15)
                .window(4, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Observable<Long>>() {
                    @Override
                    public void accept(Observable<Long> longObservable) throws Exception {
                        mRxOperatorsText.append("Sub Divide begin...\n");
                        Log.e(TAG, "Sub Divide begin...\n");
                        longObservable.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<Long>() {
                                    @Override
                                    public void accept(Long aLong) throws Exception {
                                        mRxOperatorsText.append("Next:" + aLong + "\n");
                                        Log.e(TAG, "Next:" + aLong + "\n");
                                    }
                                });
                    }
                });
    }

    //    scan 操作符作用和上面的 reduce 一致，唯一区别是 reduce 是个只追求结果的坏人，而 scan 会始终如一地把每一个步骤都输出。
    private void rxDemo_scan() {
        Observable.just(1, 2, 3, 4, 5)
                .scan(new BiFunction<Integer, Integer, Integer>() {
                    @Override
                    public Integer apply(Integer integer, Integer integer2) throws Exception {
                        return integer + integer2;
                    }
                }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                mRxOperatorsText.append("scan : " + integer + "\n");
                Log.e(TAG, "accept: scan : " + integer + "\n");
            }
        });
    }

    //    reduce 操作符每次用一个方法处理一个值，可以有一个 seed 作为初始值。
    private void rxDemo_reduce() {
        Observable.just(1, 2, 3, 4, 5)
                .reduce(new BiFunction<Integer, Integer, Integer>() {
                    @Override
                    public Integer apply(Integer integer, Integer integer2) throws Exception {
                        Log.e(TAG, integer + " " + integer2);
                        return integer + integer2;
                    }
                }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                mRxOperatorsText.append("reduce : " + integer + "\n");
                Log.e(TAG, "accept: reduce : " + integer + "\n");
            }
        });
    }

    //    merge 顾名思义，熟悉版本控制工具的你一定不会不知道 merge 命令，而在 Rx 操作符中，merge 的作用是把多个 Observable 结合起来，
//    接受可变参数，也支持迭代器集合。注意它和 concat 的区别在于，不用等到 发射器 A 发送完所有的事件再进行发射器 B 的发送
    private void rxDemo_merge() {
        Observable.merge(Observable.just(1, 2), Observable.just(3, 4, 5))
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        mRxOperatorsText.append("merge :" + integer + "\n");
                        Log.e(TAG, "accept: merge :" + integer + "\n");
                    }
                });
    }

    //    取出可观察到的最后一个值，或者是满足某些条件的最后一项
    private void rxDemo_last() {
        Observable.just(1, 2, 3)
                .last(0)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        mRxOperatorsText.append("last : " + integer + "\n");
                        Log.e(TAG, "last : " + integer + "\n");
                    }
                });
    }

    //    简单地时候就是每次订阅都会创建一个新的 Observable，并且如果没有被订阅，就不会产生新的 Observable
    private void rxDemo_defer() {
        Observable<Integer> observable = Observable.defer(new Callable<ObservableSource<? extends Integer>>() {
            @Override
            public ObservableSource<? extends Integer> call() throws Exception {
                return Observable.just(1, 2, 3);
            }
        });

        observable.subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Integer integer) {
                mRxOperatorsText.append("defer : ").append(integer).append("\n");
                Log.e(TAG, "defer : " + integer + "\n");
            }

            @Override
            public void onError(Throwable e) {
                mRxOperatorsText.append("defer : onError : ").append(e.getMessage()).append("\n");
                Log.e(TAG, "defer : onError : " + e.getMessage() + "\n");
            }

            @Override
            public void onComplete() {
                mRxOperatorsText.append("defer : onComplete\n");
                Log.e(TAG, "defer : onComplete\n");
            }
        });

    }

    //    去除发送频率过快的项
    private void rxDemo_debounce() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                // send events with simulated time wait
                emitter.onNext(1); // skip
                Thread.sleep(400);
                emitter.onNext(2); // deliver
                Thread.sleep(505);
                emitter.onNext(3); // skip
                Thread.sleep(100);
                emitter.onNext(4); // deliver
                Thread.sleep(605);
                emitter.onNext(5); // deliver
                Thread.sleep(510);
                emitter.onComplete();
            }
        }).debounce(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        mRxOperatorsText.append("debounce :" + integer + "\n");
                        Log.e(TAG, "debounce :" + integer + "\n");
                    }
                });
    }

    //    Single 只会接收一个参数，而 SingleObserver 只会调用 onError() 或者 onSuccess()
    private void rxDemo_single() {
        Single.just(new Random().nextInt())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        mRxOperatorsText.append("single : onSuccess : ").append(integer).append("\n");
                        Log.e(TAG, "single : onSuccess : " + integer + "\n");
                    }

                    @Override
                    public void onError(Throwable e) {
                        mRxOperatorsText.append("single : onError : ").append(e.getMessage()).append("\n");
                        Log.e(TAG, "single : onError : " + e.getMessage() + "\n");
                    }
                });
    }

    //    一个简单的发射器依次调用 onNext() 方法
    private void rxDemo_just() {
        Observable.just("1", "2", "3")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        mRxOperatorsText.append("accept : onNext : " + s + "\n");
                        Log.e(TAG, "accept : onNext : " + s + "\n");
                    }
                });
    }

    //    接受一个 long 型参数 count ，代表至多接收 count 个数据
    private void rxDemo_take() {
        Flowable.fromArray(1, 2, 3, 4, 5)
                .take(2)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        mRxOperatorsText.append("take : " + integer + "\n");
                        Log.e(TAG, "accept: take : " + integer + "\n");
                    }
                });
    }

    //    接受一个 long 型参数 count ，代表跳过 count 个数目开始接收
    private void rxDemo_skip() {
        Observable.just(1, 2, 3, 4, 5)
                .skip(2)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        mRxOperatorsText.append("skip : " + integer + "\n");
                        Log.e(TAG, "skip : " + integer + "\n");
                    }
                });
    }

    //    让订阅者在接收到数据之前干点有意思的事情。假如我们在获取到数据之前想先保存一下它
    private void rxDemo_doOnNext() {
        Observable.just(1, 2, 3)
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        mRxOperatorsText.append("doOnNext 保存 " + integer + "成功" + "\n");
                        Log.e(TAG, "doOnNext 保存 " + integer + "成功" + "\n");
                    }
                })
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        Log.e(TAG, "doOnSubscribe: ");
                    }
                })
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.e(TAG, "doOnTerminate: ");
                    }
                })
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        mRxOperatorsText.append("doOnNext :" + integer + "\n");
                        Log.e(TAG, "doOnNext :" + integer + "\n");
                    }
                });
    }

    //    间隔时间执行某个操作，其接受三个参数，分别是第一次发送延迟，间隔时间，时间单位
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
    private void rxDemo_distinct() {
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
    private void rxDemo_concatMap() {
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
    private void rxDemo_flatMap() {
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
