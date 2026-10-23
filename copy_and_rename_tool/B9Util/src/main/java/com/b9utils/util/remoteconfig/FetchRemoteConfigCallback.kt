package  com.b9utils.util.remoteconfig

import com.google.android.gms.tasks.Task

interface FetchRemoteConfigCallback {

    fun onCompleteListener(task: Task<Boolean>){}

    fun onFailureListener(exception: Exception){}

    fun onSuccessListener(){}

    fun onCanceledListener(){}
}