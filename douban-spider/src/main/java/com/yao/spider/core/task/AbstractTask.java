package com.yao.spider.core.task;

import com.yao.spider.core.entity.Page;
import com.yao.spider.core.http.client.BaseHttpClient;
import com.yao.spider.core.http.util.HttpClientUtil;
import com.yao.spider.core.util.ProxyUtil;
import com.yao.spider.proxytool.ProxyPool;
import com.yao.spider.proxytool.entity.Proxy;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractTask<T> implements Runnable{
    Logger logger = LoggerFactory.getLogger(AbstractTask.class);//TODO 想办法实现用子类的名称打印log

    protected boolean isUseProxy;
    protected String url;
//    protected BaseHttpClient httpClient; //具体实例化放在子类中 //TODO 这种写法错误，因为子类需要拓展新的方法这样些就无法使用拓展的方法了
    private BaseHttpClient httpClient = BaseHttpClient.getInstance();
    protected Proxy currentProxy;
    public AtomicInteger retryTimes;

    public void getPage(String url) {
        this.getPage(url, isUseProxy);
    }

    public void getPage(String url, boolean isUseProxy) {
        this.url = url;
        this.isUseProxy = isUseProxy;

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