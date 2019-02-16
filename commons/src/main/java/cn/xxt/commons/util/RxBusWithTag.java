package cn.xxt.commons.util;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * Created by Luke on 16/2/16.
 *
 * RxBus演示 用tag区分订阅者
 *
 * 与观察者模式类似，注册、注销、发送事件
 *
 */
public class RxBusWithTag {

    private static volatile RxBusWithTag rxBusInstance;

    public static synchronized RxBusWithTag getInstance() {
        if (rxBusInstance == null) {
            rxBusInstance = new RxBusWithTag();
        }

        return rxBusInstance;
    }

    private ConcurrentHashMap<Object, List<Map<String,Object>>> subjectMapper = new ConcurrentHashMap<>();

    /**
     * 注册
     *
     * @param tag 标识
     * @return 观察者
     */
    public synchronized <T> Observable<T> register(Object tag) {
        return register(tag,false);
    }

    /**
     * 提供使用一次后就销毁的observable，用后即焚
     * @param tag
     * @param <T>
     * @return
     */
    public synchronized <T> Observable<T> registerOneshot(Object tag) {
        return register(tag,true);
    }

    /**
     * 按tag和实例注销
     *
     * @param tag 标识
     * @param observable 观察者
     */
    public synchronized void unregister(Object tag,Observable observable) {
        if(tag == null || observable == null) {
            return;
        }
        List<Map<String,Object>> subjectList = subjectMapper.get(tag);

        if (subjectList!=null) {
            Iterator subjectListIt = subjectList.iterator();
            while (subjectListIt.hasNext()) {
                Map<String, Object> map = (Map<String, Object>) subjectListIt.next();
                Subject subject = (Subject) map.get("subject");
                if (observable.equals(subject)) {
                    subjectListIt.remove();
                    break;
                }
            }
        }
    }

    /**
     * 按tag注销，仅确保tag只有自己用的时候才能用这方法
     *
     * @param tag 标识
     */
    public synchronized void clear(Object tag) {
        if (tag != null) {
            subjectMapper.remove(tag);
        }
    }

    public synchronized boolean send(Object tag, Object content) {
        if (tag == null || content == null) {
            return false;
        }
        List<Map<String,Object>> subjectList = subjectMapper.get(tag);
        boolean flag = false;
        if (subjectList != null && subjectList.size() != 0) {
            Iterator subjectListIt = subjectList.iterator();
            while (subjectListIt.hasNext()) {
                Map<String, Object> map = (Map<String, Object>) subjectListIt.next();
                Subject subject = (Subject) map.get("subject");
                boolean isOneshot = (boolean) map.get("isOneshot");
                if (subject.hasObservers()) {
                    subject.onNext(content);
                    flag = true;
                }
                if (isOneshot) {
                    //执行onCompleted会自动取消订阅
                    subject.onCompleted();
                    subjectListIt.remove();
                }
            }
        }
        return flag;
    }

    /**
     * 清除无用的Subject
     */
    public synchronized void cleanUnusedSubject() {
        Iterator it = subjectMapper.keySet().iterator();
        while(it.hasNext())
        {
            String tag = (String)it.next();
            List<Map<String,Object>> subjectList = subjectMapper.get(tag);

            if (subjectList!=null) {
                Iterator subjectListIt = subjectList.iterator();
                while (subjectListIt.hasNext()) {
                    Map<String, Object> map = (Map<String, Object>) subjectListIt.next();
                    Subject subject = (Subject) map.get("subject");
                    if (!subject.hasObservers()) {
                        subjectListIt.remove();
                    }
                }
                if (subjectList.size() == 0) {
                    it.remove();
                }
            }
        }
    }


    private synchronized <T> Observable<T> register(@NonNull Object tag, boolean isOneshot) {
        List<Map<String,Object>> subjectList = subjectMapper.get(tag);
        if (null == subjectList) {
            subjectList = new ArrayList<>();
            subjectMapper.put(tag, subjectList);
        }

        Subject<T, T> subject = PublishSubject.create();

        Map<String,Object> map = new HashMap<>();
        map.put("subject",subject);
        map.put("isOneshot",isOneshot);
        subjectList.add(map);

        return subject;
    }
}
