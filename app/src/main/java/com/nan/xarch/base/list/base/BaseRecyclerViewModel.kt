package com.nan.xarch.base.list.base

import androidx.lifecycle.MutableLiveData
import com.nan.xarch.base.BaseViewModel
import com.nan.xarch.bean.LoadError
import com.nan.xarch.util.isNetworkConnect
import java.util.concurrent.atomic.AtomicInteger

abstract class BaseRecyclerViewModel : BaseViewModel() {

    /**
     * 首页/下拉刷新的数据
     */
    val firstViewDataLiveData = MutableLiveData<List<BaseViewData<*>>>()

    /**
     * 更多的数据
     */
    val moreViewDataLiveData = MutableLiveData<List<BaseViewData<*>>>()

    /**
     * 页码
     */
    private var currentPage = AtomicInteger(0)

    /**
     * 游标偏移
     */
    private var currentOffset = AtomicInteger(0)

    /**
     * 子类重写这个函数加载数据
     * 数据加载完成后通过postData提交数据
     * 数据加载完成后通过postError提交异常
     *
     * @param isLoadMore 当次是否为加载更多
     * @param isReLoad   当次是否为重新加载(此时page等参数不应该改变)
     */
    abstract fun loadData(isLoadMore: Boolean, isReLoad: Boolean, page: Int, offset: Int)

    fun loadDataInternal(isLoadMore: Boolean, isReLoad: Boolean) {
        if (needNetwork() && !isNetworkConnect()) {
            postError(isLoadMore)
            return
        }
        if (!isLoadMore) {
            currentPage.set(0)
            currentOffset.set(0)
        } else if (!isReLoad) {
            currentPage.incrementAndGet()
            currentOffset.addAndGet(getPageSize())
        }
        loadData(isLoadMore, isReLoad, currentPage.get(), currentOffset.get())
    }

    /**
     * 每一页的数据大小，默认为10条
     */
    open fun getPageSize(): Int {
        return 10
    }

    /**
     * 获取当前页码
     */
    fun getCurrentPage(): Int {
        return currentPage.get()
    }

    /**
     * 子类可以实现这个方法,返回加载数据是否需要网络
     */
    open fun needNetwork(): Boolean {
        return true
    }

    /**
     * 首次加载如果少于pageSize是否继续加载
     */
    open fun firstLoadLessContinue(): Boolean {
        return true
    }

    /**
     * 提交数据
     */
    protected fun postData(isLoadMore: Boolean, viewData: List<BaseViewData<*>>) {
        if (isLoadMore) {
            moreViewDataLiveData.postValue(viewData)
        } else {
            firstViewDataLiveData.postValue(viewData)
        }
    }

    /**
     * 提交加载异常
     */
    protected fun postError(isLoadMore: Boolean) {
        if (isLoadMore) {
            moreViewDataLiveData.postValue(LoadError)
        } else {
            firstViewDataLiveData.postValue(LoadError)
        }
    }
}