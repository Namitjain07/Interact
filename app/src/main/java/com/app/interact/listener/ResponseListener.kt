package com.app.interact.listener

interface ResponseListener<T> {
    fun onResponse(response: T?)
}