package com.interact.interact.listener

interface ResponseListener<T> {
    fun onResponse(response: T?)
}