#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_b9ads_secret_Secret_gk(
        JNIEnv* env,
        jobject /* this */) {
    std::string key = "21f24ce47eb338cc666d344d438ec437";
    return env->NewStringUTF(key.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_b9ads_secret_Secret_iv(
        JNIEnv* env,
        jobject /* this */) {
    std::string iv = "fed2bb7f32abe4cd361a28a9051475b9";
    return env->NewStringUTF(iv.c_str());
}
