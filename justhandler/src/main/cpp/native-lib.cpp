#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_sunshine_justhandler_MessageFactory_00024Companion_stringFromJNI(
        JNIEnv *env, jobject /* this */
) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

//SynchronizationContext mainThreadSynContext = SynchronizationContext.Current;
//Action action = null;
//Console.WriteLine($
//"主线程ID：{Thread.CurrentThread.ManagedThreadId}");
//action += () => {
//Console.WriteLine($
//"委托线程ID：{Thread.CurrentThread.ManagedThreadId}");
//};
//Task.Run(() = > {
//Console.WriteLine($"Task线程ID：{Thread.CurrentThread.ManagedThreadId}");
//action?.Invoke();
//mainThreadSynContext.Send(new SendOrPostCallback((ss) => {
//Console.WriteLine($
//"听说是主线程ID：{Thread.CurrentThread.ManagedThreadId}");
//}), null);
////通知主线程
//s(this.Invoke);
//});
//
//void s(Func _action) {
//    _action.Invoke(new Action(() = > {
//            Console.WriteLine($"窗体控件线程ID：{Thread.CurrentThread.ManagedThreadId}");
//    }));
//}