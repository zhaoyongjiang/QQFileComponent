package cn.xxt.commons.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import cn.xxt.commons.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import tech.linjiang.pandora.Pandora;

/**
 * 调用远程API服务接口工具类
 */
public final class RemoteServiceUtil {

    public static final String TASK_REMOTE_SERVICE_UTIL = "RemoteServiceUtil.task";

    /** 默认请求超时设置-连接超时时间(单位：秒) */
    private static final long DEFAULT_CONNECTION_TIMEOUT_SECONDS = 30;

    /** 默认请求超时设置-读超时时间(单位：秒) */
    private static final long DEFAULT_READ_TIMEOUT_SECONDS = 30;

    /** 默认请求超时设置-写超时时间(单位：秒) */
    private static final long DEFAULT_WRITE_TIMEOUT_SECONDS = 30;

    /** HTTP请求头之请求来源 */
    private static final String HTTP_HEADER_REFERER = "Refer";

    /** HTTP请求头之客户端类型 */
    private static final String HTTP_HEADER_USER_AGENT = "User-Agent";

    /** HTTP请求头之请求的数据类型 */
    private static final String HTTP_HEADER_ACCEPT_TYPE = "Accept";

    private static final String HTTP_HEADER_CONNECTION = "Connection";

    private static ClearableCookieJar cookieJarInstance = null;

    /**
     * 创建Retrofit实例(使用默认超时时间)
     * @param context   context上下文
     * @param endPoint  指定API服务URL，格式如"http://login.xxt.cn"
     * @return          Retrofit实例
     */
    public static Retrofit createRetrofitInstance(Context context, String endPoint) {
        return createRetrofitInstance(context, endPoint,
                DEFAULT_CONNECTION_TIMEOUT_SECONDS, DEFAULT_READ_TIMEOUT_SECONDS, DEFAULT_WRITE_TIMEOUT_SECONDS);
    }

    /**
     * 创建Retrofit实例(可设置超时时间)
     * @param context               context上下文
     * @param endPoint              指定API服务URL，格式如"http://login.xxt.cn"
     * @param connectTimeout        连接超时时间，传null时使用默认设置{@link #DEFAULT_CONNECTION_TIMEOUT_SECONDS}
     * @param readTimeoutSeconds    读超时时间，传null时使用默认设置{@link #DEFAULT_READ_TIMEOUT_SECONDS}
     * @param writeTimeoutSeconds   写超时时间写超时时间，传null时使用默认设置{@link #DEFAULT_WRITE_TIMEOUT_SECONDS}
     * @return                      Retrofit实例
     */
    public static Retrofit createRetrofitInstance(
            Context context,
            String endPoint,
            Long connectTimeout,
            Long readTimeoutSeconds,
            Long writeTimeoutSeconds) {

        OkHttpClient okHttpClient = getOkHttpClient(context, connectTimeout,
                readTimeoutSeconds, writeTimeoutSeconds);

        Gson gson = new GsonBuilder()
                .serializeNulls()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create();
        endPoint = StringUtil.urlStr2HttpsUrlStr(endPoint);
        return new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(endPoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
    }

    /**
     * 创建OkHttpClient实例
     * @param context
     * @return OkHttpClient
     */
    public static synchronized OkHttpClient getOkHttpClient(Context context) {
        return getOkHttpClient(context, DEFAULT_CONNECTION_TIMEOUT_SECONDS,
                DEFAULT_READ_TIMEOUT_SECONDS, DEFAULT_WRITE_TIMEOUT_SECONDS);
    }

    /**
     * 创建OkHttpClient实例
     * @param context
     * @param connectTimeout
     * @param readTimeoutSeconds
     * @param writeTimeoutSeconds
     * @return OkHttpClient
     */
    public static OkHttpClient getOkHttpClient(
            Context context,
            Long connectTimeout,
            Long readTimeoutSeconds,
            Long writeTimeoutSeconds) {

        OkHttpClient okHttpClient;
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(new OkHttpInterceptor(context))
                .cookieJar(getCookieJarInstance(context))
                .retryOnConnectionFailure(false)
                .connectTimeout(
                        connectTimeout != null ? connectTimeout : DEFAULT_CONNECTION_TIMEOUT_SECONDS,
                        TimeUnit.SECONDS)
                .readTimeout(readTimeoutSeconds != null ? readTimeoutSeconds : DEFAULT_READ_TIMEOUT_SECONDS,
                        TimeUnit.SECONDS)
                .writeTimeout(writeTimeoutSeconds != null ? writeTimeoutSeconds : DEFAULT_WRITE_TIMEOUT_SECONDS,
                        TimeUnit.SECONDS);

        try {
            if (false) {
                Pandora pandora = null;
                try {
                    pandora = Pandora.get();
                } catch (Exception e) {
                    try {
                        pandora = Pandora.init(((Activity) context).getApplication());
                    } catch (Exception e1) {

                    }
                }

                if (pandora != null) {
                    builder.addInterceptor(pandora.getInterceptor());
                }
            }
        } catch (Exception e) {

        }

        okHttpClient = builder.build();

        return okHttpClient;
    }

    /**
     *  获取无拦截的okhttpclient
     * @param context
     * @return
     */
    public static OkHttpClient getOkHttpClientNoInterceptor(Context context,
                                                            Long connectTimeout,
                                                            Long readTimeoutSeconds,
                                                            Long writeTimeoutSeconds) {

        OkHttpClient okHttpClient;
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .cookieJar(getCookieJarInstance(context))
                .retryOnConnectionFailure(false)
                .connectTimeout(
                        connectTimeout != null ? connectTimeout : DEFAULT_CONNECTION_TIMEOUT_SECONDS,
                        TimeUnit.SECONDS)
                .readTimeout(readTimeoutSeconds != null ? readTimeoutSeconds : DEFAULT_READ_TIMEOUT_SECONDS,
                        TimeUnit.SECONDS)
                .writeTimeout(writeTimeoutSeconds != null ? writeTimeoutSeconds : DEFAULT_WRITE_TIMEOUT_SECONDS,
                        TimeUnit.SECONDS);
        try {
            if (false) {
                Pandora pandora = null;
                try {
                    pandora = Pandora.get();
                } catch (Exception e) {
                    try {
                        pandora = Pandora.init(((Activity) context).getApplication());
                    } catch (Exception e1) {

                    }
                }

                if (pandora != null) {
                    builder.addInterceptor(pandora.getInterceptor());
                }
            }
        } catch (Exception e) {

        }

        okHttpClient = builder.build();

        return okHttpClient;
    }



    /***
     * 请求参数不需要编码的请求
     * @param ctx       上下文
     * @param method    请求方法：GET、POST
     * @param url       请求url：不带查询参数的形式
     * @param parasMap  请求参数
     * @return          请求结果
     */
    public static Observable<String> okhttpRequest(Context ctx, String method, String url,
                                                   Map<String, Object> parasMap) {
        return okhttpRequestWithEncoding(ctx, method, url, parasMap, null);
    }

    /***
     * 请求参数走默认编码的请求
     * @param ctx       上下文
     * @param method    请求方法：GET、POST
     * @param url       请求url
     * @param parasMap  请求参数
     * @return          请求结果
     */
    public static Observable<String> okhttpRequestWithDefaultEncoding(Context ctx, String method, String url,
                                                                      Map<String, Object> parasMap) {
        return okhttpRequestWithEncoding(ctx, method, url, parasMap, "utf-8");
    }

    /***
     * 请求参数需要指定编码方式的请求
     * @param ctx       上下文
     * @param method    请求方法：GET、POST
     * @param url       请求url：不带查询参数的形式
     * @param parasMap  请求参数
     * @param encoding  请求参数的编码格式.当encoding为null或者""时，则不再编码
     * @return          请求结果
     */
    public static Observable<String> okhttpRequestWithEncoding(Context ctx, String method, final String url,
                                                               Map<String, Object> parasMap, final String encoding) {
        final Context context = ctx;
        final String requestMethod = method;
        final String urlStr = url;
        final Map<String, Object> paras = parasMap;

        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                try {
                    OkHttpClient client = getOkHttpClient(context,null,null,null);

                    FormBody.Builder builder = new FormBody.Builder();

                    Request request;
                    if ("GET".equals(requestMethod.toUpperCase())) {
                        // GET请求
                        String urlPath = urlStr;
                        //http 转 https
                        urlPath = StringUtil.urlStr2HttpsUrlStr(urlPath);
                        // 处理GET请求的参数
                        if (paras != null && paras.size() > 0) {
                            String parasStr = "";
                            for (String key : paras.keySet()) {

                                String keyValue = paras.get(key).toString();
                                if (encoding != null && !"".equals(encoding)) {
                                    keyValue = URLEncoder.encode(keyValue, encoding);
                                }
                                if ("".equals(parasStr)) {
                                    parasStr = StringUtil.connectStrings(parasStr, key, "=", keyValue);
                                } else {
                                    parasStr = StringUtil.connectStrings(parasStr, "&", key, "=", keyValue);
                                }
                            }

                            if (!"".equals(parasStr)) {
                                if (urlStr.contains("?")) {
                                    urlPath = StringUtil.connectStrings(urlPath, "&", parasStr);
                                } else {
                                    urlPath = StringUtil.connectStrings(urlPath, "?", parasStr);
                                }
                            }
                        }
                        request = new Request.Builder()
                                .url(urlPath)
                                .build();
                    } else {
                        // POST请求

                        Uri url = Uri.parse(urlStr);
                        String urlPath = StringUtil.connectStrings("http://", url.getHost(), url.getPath());
                        urlPath = StringUtil.urlStr2HttpsUrlStr(urlPath);

                        // 解析url中的查询参数
                        String queryStr = url.getQuery();
                        if (queryStr != null && !"".equals(queryStr)) {

                            String[] querys = queryStr.split("&");
                            for (String query : querys) {
                                if (query.contains("=")) {
                                    String[] keyValue = query.split("=");
                                    String value = keyValue[1];
                                    if (encoding != null && !"".equals(encoding)) {
                                        value = URLEncoder.encode(value, encoding);
                                    }
                                    builder.addEncoded(keyValue[0], value);
                                }
                            }
                        }

                        // 解析传递过来的查询参数
                        if (paras != null && paras.size() > 0) {
                            for (String key : paras.keySet()) {
                                Object valueObject = paras.get(key);
                                String value = valueObject ==null? "" : valueObject.toString();
                                if (encoding != null && !"".equals(encoding)) {
                                    value = URLEncoder.encode(value, encoding);
                                }
                                builder.addEncoded(key, value);
                            }
                        }

                        RequestBody postBody = builder.build();

                        request = new Request.Builder()
                                .url(urlPath)
                                .post(postBody)
                                .build();
                    }

                    // 服务端接口请求前日志
                    final String reqUrl = getUrlWithMethod(context, request);
                    final long beginTime = System.currentTimeMillis();


                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            subscriber.onError(e);
                            // 接口请求失败的日志
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                //看日志会偶现IllegalStateException，查看是因为调用了string()和bytes()会关闭response，
                                //但我们没有用这两个，安全起见，还是拷贝出来用
                                Response responseCopy = response.newBuilder().build();
                                Response responseCopy2 = response.newBuilder().build();

                                InputStream input = responseCopy.body().byteStream();
                                MediaType contentType = responseCopy2.body().contentType();
                                String responseStr = "";
                                if (contentType != null && contentType.charset() != null) {
                                    responseStr = CharStreams.toString(new InputStreamReader(input, contentType.charset().name()));
                                } else {
                                    responseStr = StringUtil.inputStreamToString(input);
                                }

                                input.close();

                                //备注：h5接口网络请求支持jsonarray。修改接口返回jsonobject为string，订阅的地方强转接口返回的类型  20180312
                                //                                Gson gson = new Gson();
                                //                                JsonObject jsonObject = gson.fromJson(responseStr, JsonObject.class);
                                subscriber.onNext(responseStr);
                            } catch (RemoteServiceException re) {
                                subscriber.onError(re);
                            } catch (IOException e) {
                                subscriber.onError(e);
                            } catch (Exception e) {
                                subscriber.onError(e);
                            }
                        }
                    });
                } catch (UnsupportedEncodingException e) {
                    subscriber.onError(e);
                }catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    /**
     * parasMap中的字段，如果是file，直接post，其他的参数都拼接到url后边，这个方法专门用来处理类似文件上传图片那种接口
     * 即文件+参数，参数用@Query修饰，file用@Part(doc) RequestBody 修饰
     * @param ctx
     * @param method
     * @param url
     * @param parasMap
     * @param encoding
     * @return
     */
    public static Observable<String> okhttpRequestWithEncodingAndFile(Context ctx, String method, final String url,
                                                                      Map<String, Object> parasMap, final String encoding) {
        final Context context = ctx;
        final String requestMethod = method;
        final String urlStr = url;
        final Map<String, Object> paras = parasMap;

        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                try {
                    OkHttpClient client = getOkHttpClient(context);

                    //                    // 构建 OkHttpClient 时,将 OkHttpClient.Builder() 传入 with() 方法,进行初始化配置
                    //                    client = ProgressManager.getInstance().with(new OkHttpClient.Builder())
                    //                            .build();

                    MultipartBody.Builder builder = new MultipartBody.Builder();
                    RequestBody postBody = null;

                    Request request;
                    if ("GET".equals(requestMethod.toUpperCase())) {
                        // GET请求
                        String urlPath = urlStr;
                        //http 转 https
                        urlPath = StringUtil.urlStr2HttpsUrlStr(urlPath);
                        // 处理GET请求的参数
                        if (paras != null && paras.size() > 0) {
                            String parasStr = "";
                            for (String key : paras.keySet()) {

                                String keyValue = paras.get(key).toString();
                                if (encoding != null && !"".equals(encoding)) {
                                    keyValue = URLEncoder.encode(keyValue, encoding);
                                }
                                if ("".equals(parasStr)) {
                                    parasStr = StringUtil.connectStrings(parasStr, key, "=", keyValue);
                                } else {
                                    parasStr = StringUtil.connectStrings(parasStr, "&", key, "=", keyValue);
                                }
                            }

                            if (!"".equals(parasStr)) {
                                if (urlStr.contains("?")) {
                                    urlPath = StringUtil.connectStrings(urlPath, "&", parasStr);
                                } else {
                                    urlPath = StringUtil.connectStrings(urlPath, "?", parasStr);
                                }
                            }
                        }
                        request = new Request.Builder()
                                .url(urlPath)
                                .build();
                    } else {
                        // POST请求

                        Uri url = Uri.parse(urlStr);
                        String urlPath = StringUtil.connectStrings("http://", url.getHost(), url.getPath());
                        urlPath = StringUtil.urlStr2HttpsUrlStr(urlPath);


                        // 解析传递过来的查询参数
                        if (paras != null && paras.size() > 0) {
                            String parasStr = "";
                            for (String key : paras.keySet()) {

                                String keyValue = paras.get(key).toString();
                                Object valueObject = paras.get(key);
                                if (valueObject instanceof RequestBody) {
                                    // 问文件形式参数
                                    postBody = (RequestBody) valueObject;
                                } else {
                                    if (encoding != null && !"".equals(encoding)) {
                                        keyValue = URLEncoder.encode(keyValue, encoding);
                                    }
                                    if ("".equals(parasStr)) {
                                        parasStr = StringUtil.connectStrings(parasStr, key, "=", keyValue);
                                    } else {
                                        parasStr = StringUtil.connectStrings(parasStr, "&", key, "=", keyValue);
                                    }
                                }
                            }
                            if (!"".equals(parasStr)) {
                                if (urlStr.contains("?")) {
                                    urlPath = StringUtil.connectStrings(urlPath, "&", parasStr);
                                } else {
                                    urlPath = StringUtil.connectStrings(urlPath, "?", parasStr);
                                }
                            }
                        }

                        //                        RequestBody postBody = builder.build();

                        request = new Request.Builder()
                                .url(urlPath)
                                .post(postBody)
                                .build();
                    }

                    // 服务端接口请求前日志
                    final String reqUrl = getUrlWithMethod(context, request);
                    final long beginTime = System.currentTimeMillis();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            subscriber.onError(e);
                            // 接口请求失败的日志
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                //看日志会偶现IllegalStateException，查看是因为调用了string()和bytes()会关闭response，
                                //但我们没有用这两个，安全起见，还是拷贝出来用
                                Response responseCopy = response.newBuilder().build();
                                Response responseCopy2 = response.newBuilder().build();

                                InputStream input = responseCopy.body().byteStream();
                                MediaType contentType = responseCopy2.body().contentType();
                                String responseStr = "";
                                if (contentType != null && contentType.charset()!=null) {
                                    responseStr = CharStreams.toString(new InputStreamReader(input, contentType.charset().name()));
                                } else {
                                    responseStr = StringUtil.inputStreamToString(input);
                                }

                                input.close();

                                //备注：h5接口网络请求支持jsonarray。修改接口返回jsonobject为string，订阅的地方强转接口返回的类型  20180312
                                //                                Gson gson = new Gson();
                                //                                JsonObject jsonObject = gson.fromJson(responseStr, JsonObject.class);
                                subscriber.onNext(responseStr);
                            } catch (IOException e) {
                                subscriber.onError(e);
                            } catch (Exception e) {
                                subscriber.onError(e);
                            }
                        }
                    });
                } catch (UnsupportedEncodingException e) {
                    subscriber.onError(e);
                }catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static synchronized ClearableCookieJar getCookieJarInstance(Context context) {
        if (cookieJarInstance == null || cookieJarInstance.isNull()) {
            cookieJarInstance =  new PersistentCookieJar(new SetCookieCache(),
                    new SharedPrefsCookiePersistor(context));
        }

        return cookieJarInstance;
    }

    private static class OkHttpInterceptor implements Interceptor {

        Context context;
        public OkHttpInterceptor(Context context) {
            this.context = context;
        }

        @Override
        public Response intercept(Chain chain) throws IOException{

            Request originalRequest = chain.request();
            Request.Builder requestBuilder = originalRequest.newBuilder();

            //XXX:每个工程的配置应该不一样
            if (null == originalRequest.header(HTTP_HEADER_REFERER)) {
                requestBuilder.header(HTTP_HEADER_REFERER, "android.jxq.xxt.cn");
            }

            if (null == originalRequest.header(HTTP_HEADER_USER_AGENT)) {
                // 客户端类型需要更多信息
                requestBuilder.header(HTTP_HEADER_USER_AGENT, getUserAgent(context));
            }

            if (null == originalRequest.header(HTTP_HEADER_ACCEPT_TYPE)) {
                requestBuilder.header(HTTP_HEADER_ACCEPT_TYPE, "application/json");
            }

            //            if (null == originalRequest.header(HTTP_HEADER_CONNECTION)) {
            //                requestBuilder.header(HTTP_HEADER_CONNECTION,"close");
            //            }

            // 服务端接口请求前日志
            String reqUrl = getUrlWithMethod(context, originalRequest);
            long beginTime = System.currentTimeMillis();
            // 请求接口
            Response response;
            try {
                response = chain.proceed(requestBuilder.build());

            } catch (RemoteServiceException re) {
                // 继续抛
                throw re;
            } catch (Exception e) {
                if (e instanceof SSLException) {
                    //ssl 网络异常
                    SwitchOfHttps.setCloseHttps(true);
                }

                e.printStackTrace();

                String detailMsg = "exception";
                if (e.getCause()!=null && e.getCause().getMessage()!=null) {
                    detailMsg = e.getCause().getMessage();
                } else if (e.getMessage()!=null) {
                    detailMsg = e.getMessage();
                }

                throw new IOException(detailMsg);
            }

            return response;
        }
    }

    public static void copyXXTCookieToJXTAndLexue(Context context) {
        ClearableCookieJar cookieJar = getCookieJarInstance(context);

        if (cookieJar instanceof PersistentCookieJar) {
            List<Cookie> cookieList = cookieJar.loadForRequest(HttpUrl.parse("https://xxt.cn"));

            //处理cookie
            List<Cookie> jxtCookieList = new ArrayList<>();
            List<Cookie> lexueCookieList = new ArrayList<>();

            for (Cookie cookie : cookieList) {
                Cookie newJxtCookie = new Cookie.Builder()
                        .name(cookie.name())
                        .value(cookie.value())
                        .domain("hbjxt.cn")
                        .path(cookie.path())
                        .build();

                Cookie newLexueCookie = new Cookie.Builder()
                        .name(cookie.name())
                        .value(cookie.value())
                        .domain("lexue.cn")
                        .path(cookie.path())
                        .build();

                jxtCookieList.add(newJxtCookie);
                lexueCookieList.add(newLexueCookie);
            }

            cookieJar.saveFromResponse(HttpUrl.parse("https://login.hbjxt.cn"), jxtCookieList);
            cookieJar.saveFromResponse(HttpUrl.parse("https://login.lexue.cn"), lexueCookieList);
            //            cookieJar.saveFromResponse(HttpUrl.parse("https://login.hbjxt.cn"), jxtCookieList);
            //            cookieJar.saveFromResponse(HttpUrl.parse("https://login.lexue.cn"), lexueCookieList);
        }
    }

    /**
     * 获取异常的status，非自定义异常返回-1
     * @param e 异常
     * @return  异常status
     */
    public static int getExceptionStatus(Throwable e) {
        int exceptionStatus = -1;
        if (e instanceof RemoteServiceException) {
            exceptionStatus = ((RemoteServiceException) e).getStatus();
        }
        return exceptionStatus;
    }

    /**
     * 获取异常的code，非自定义异常返回-1
     * @param e 异常
     * @return  异常code
     */
    public static int getExceptionCode(Throwable e) {
        int exceptionCode = -1;
        if (e instanceof RemoteServiceException) {
            exceptionCode = ((RemoteServiceException) e).getCode();
        }
        return exceptionCode;
    }

    /**
     * 获取异常的message，非自定义的返回“”
     * @param e 异常
     * @return  异常message
     */
    public static String getExceptionMessage(Throwable e) {
        String exceptionMessage = "";
        if (e instanceof RemoteServiceException) {
            exceptionMessage =  e.getMessage();
        }
        return exceptionMessage;
    }

    /**
     * 显示服务端请求结果的错误信息
     * @param e
     * @param context
     */
    public static void showRequestErrorInfo(Context context, Throwable e) {
        int status = getExceptionStatus(e);
        if (status < 0) {
            //网络连接失败
            ToastUtil.displayToastLong(context,
                    context.getString(R.string.commons_connect_fail_and_retry));
        } else if (401 == status || 500 == status) {
            //401 没有权限  500 业务处理逻辑 都以服务端返回的为主
            ToastUtil.displayToastLong(context, e.getMessage());
        } else if (400 == status) {
            ////需要统一处理
        } else {

        }
    }

    /**
     * 清除cookie
     * @param context
     */
    public static synchronized void clearCookie(Context context) {
        getCookieJarInstance(context).clear();
    }

    public static boolean checkIfNeedLogin(String responseStr, Context context) {
        boolean needLogin = false;
        try {
            JSONObject jo = new JSONObject(responseStr);

            if (jo.optString("_rc") != null && "login".equals(jo.optString("_rc"))) {
                needLogin = true;
            } else if (jo.optString("status") != null
                    && 401 == jo.optInt("status")
                    && jo.optString("code") != null
                    && 2 == jo.optInt("code")) {
                needLogin = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return needLogin;
    }

    public static void gotoAutoLogin(final Context context) {

    }

    /**
     * 拦截接口请求结果，识别服务端自定义异常
     * @param response  请求结果
     * @return          请求体
     * @throws IOException  异常
     */
    private static ResponseBody interceptResponse(Response response, Context context) throws Exception{
        //看日志会偶现IllegalStateException，查看是因为调用了string()和bytes()会关闭response，
        //但我们没有用这两个，安全起见，还是拷贝出来用
        Response responseCopy = response.newBuilder().build();
        Response responseCopy2 = response.newBuilder().build();

        // 读取拦截的响应体
        InputStream input = responseCopy.body().byteStream();

        MediaType contentType = responseCopy2.body().contentType();
        String responseStr = "";
        if (contentType != null && contentType.charset() != null) {
            responseStr = CharStreams.toString(new InputStreamReader(input, contentType.charset().name()));
        } else {
            responseStr = StringUtil.inputStreamToString(input);
        }

        input.close();
        JSONObject jsonObject = new JSONObject();
        if (StringUtil.isEmpty(responseStr)) {
            // 返回空字符串，转换成"content":"success"
            try {
                jsonObject.put("content", "success");
            } catch (JSONException e) {
                throw new RemoteServiceException(e.getMessage(), 500, 100);
            }
            responseStr = jsonObject.toString();
        } else if (!StringUtil.isJSON(responseStr)) {
            // 返回非JsonObject和JsonArray
            try {
                jsonObject.put("content",responseStr);
            } catch (JSONException e) {
                throw new RemoteServiceException(e.getMessage(), 500, 100);
            }
            responseStr = jsonObject.toString();
        }

        if (responseStr!= null) {
            // 检测接口返回的是不是_rc:login
            if (checkIfNeedLogin(responseStr, context)) {
                gotoAutoLogin(context);
            }
            if (responseStr.contains("status") && responseStr.contains("message")
                    && responseStr.contains("code")) {
                // 服务端返回的异常数据，抛出异常
                try {
                    jsonObject = new JSONObject(responseStr);
                    throw new RemoteServiceException(jsonObject.optString("message"),
                            jsonObject.optInt("status"), jsonObject.optInt("code"));
                } catch (JSONException e) {
                    //  JSONException异常的异常码需要制定一下:100
                    throw new RemoteServiceException(e.getMessage(), 500, 100);
                }
            }
        }


        return ResponseBody.create(contentType, responseStr);
    }

    /**
     * 获取请求url
     * @param context           上下文
     * @param originalRequest   请求
     * @return                  url、请求方式和body组成的串
     * @throws IOException      抛出io异常
     */
    private static String getUrlWithMethod(Context context, Request originalRequest) {

        // 请求url
        String reqUrl = "";

        try {
            // url
            String url = originalRequest.url().toString();
            // 请求方式
            String method = originalRequest.method();
            // 请求body
            //            RequestBody body = originalRequest.body();
            // 拼装请求方式
            if (url.contains("?")) {
                reqUrl = StringUtil.connectStrings(url, "&method=", method);
            } else {
                reqUrl = StringUtil.connectStrings(url, "?method=", method);
            }
        } catch (Exception e) {
            // 记录日志
            e.printStackTrace();
        }

        return reqUrl;
    }

    /**
     * 从文件中读取请求体内容，只读取一行即可
     * @param file          文件
     * @return              请求体内容
     * @throws IOException  异常
     */
    private static String readBodyFromFile(File file) throws IOException{
        String fileContentStr = "";
        FileInputStream fis = new FileInputStream(file);
        InputStreamReader inputReader = new InputStreamReader(fis);
        BufferedReader buffReader = new BufferedReader(inputReader);
        String line;
        // 只读一行
        if ((line = buffReader.readLine()) != null) {
            fileContentStr = line;
        }
        buffReader.close();
        inputReader.close();
        fis.close();

        return fileContentStr;
    }

    /**
     * 组织userAgent内容：android-jxq;hostId:1;appversion:7.0.2;appversioncode:702
     * @param context   上下文
     * @return          userAgent
     */
    private static String getUserAgent(Context context) {
        String userAgent = "android-jxq";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            int hostId = MetaUtil.getMetaIntValue(context,MetaUtil.HOST_ID);;
            userAgent = StringUtil.connectStrings(userAgent, ";hostId:", String.valueOf(hostId),
                    ";appversion:", pi.versionName, ";appversioncode:", String.valueOf(pi.versionCode));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return userAgent;
    }
}
