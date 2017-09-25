package cash.andrew.coffee

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers


val <T> Observable<T>.ioToMainScheduler : Observable<T> get() = this.subscribeOn(io.reactivex.schedulers.Schedulers.io()).observeOn(AndroidSchedulers.mainThread())