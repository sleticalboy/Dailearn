package com.sleticalboy.learning.data

/**
 * Created on 20-3-31.
 *
 * @author binlee sleticalboy@gmail.com
 */
open class Result<T> {

  class Success<Data>(private val mData: Data) : Result<Data>() {
    fun getData(): Data {
      return mData
    }
  }

  inner class Error(private val mCause: Throwable) : Result<T>() {
    fun getCause(): Throwable = mCause
  }

  inner class Loading : Result<T>() {
    fun getProgress(): String = "Loading..."
  }

  override fun toString(): String {
    if (this is Success<*>) {
      return "data = " + (this as Success<T>).getData()
    }
    if (this is Error) {
      return "error = " + getCause()
    }
    return if (this is Loading) {
      return getProgress()
    } else super.toString()
  }
}