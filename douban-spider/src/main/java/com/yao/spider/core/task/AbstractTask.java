package com.yao.spider.core.task;

import com.yao.spider.core.entity.Page;
import com.yao.spider.core.entity.RequestParams;
import com.yao.spider.core.http.client.BaseHttpClient;
import com.yao.spider.core.http.util.HttpClientUtil;
import com.yao.spider.core.util.ProxyUtil;
import com.yao.spider.douban.task.DouBanInfoListPageTask;
import com.yao.spider.proxytool.ProxyPool;
import com.yao.spider.proxytool.entity.Proxy;
import com.yao.spider.zhihu.task.ZhiHuUserListTask;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractTask<T> implements Runnable{
    private static Logger logger = null;//TODO 想办法实现用子类的名称打印log

    protected boolean isUseProxy;
    protected String url;
    protected BaseHttpClient httpClient = BaseHttpClient.getInstance();
    protected Proxy currentProxy;
    protected int retryTimes;

/*    protected void getPage(String url) {
        this.getPage(url, isUseProxy);
    }*/

    public AbstractTask() {
        //通过反射获取泛型的类类型
        Class<T> entityClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        //这里是为了在抽象类中打印日志时可以显示其子类名，这样有利于错误排查
        logger = LoggerFactory.getLogger(entityClass);
    }

    @Deprecated
    public void getPage(RequestParams requestParams) {

    }

    protected void getPage(String url) {
        HttpGet request = new HttpGet(url);
        try {
            Page page = null;
            if (isUseProxy) {
                currentProxy = ProxyPool.proxyQueue.take();
                HttpHost proxy = new HttpHost(currentProxy.getIp(), currentProxy.getPort());
                request.setConfig(HttpClientUtil.getRequestConfigBuilder().setProxy(proxy).build());
                page = httpClient.getPage(request);
            } else {
                page = httpClient.getPage(url);
            }
            if (page != null && page.getStatusCode() == 200) {
                if (currentProxy != null)
                    currentProxy.setSuccessfulTimes(currentProxy.getSuccessfulTimes() + 1);
                handle(page);
            } else {
                currentProxy.setFailureTimes(currentProxy.getFailureTimes() + 1);
                retry();
            }
        } catch (Exception e) {
            currentProxy.setFailureTimes(currentProxy.getFailureTimes() + 1);
            retry();
        } finally {
            if (request != null) {
                request.releaseConnection();
            }

            if (currentProxy != null && !ProxyUtil.isDiscardProxy(currentProxy)) {
                ProxyPool.proxyQueue.add(currentProxy);
            } else {
                if (currentProxy != null)
                    logger.info("丢弃代理：" + currentProxy.getProxyStr());
            }
        }
    }

    public abstract void retry();

    public abstract void handle(Page page);


}
