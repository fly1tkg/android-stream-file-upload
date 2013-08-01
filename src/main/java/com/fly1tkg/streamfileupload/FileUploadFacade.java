/*
 * Copyright (C) 2013 fly1tkg
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fly1tkg.streamfileupload;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.Handler;

public class FileUploadFacade {
    private enum HttpMethod {
        POST, PUT
    }

    private static final String DEFAULT_FILE_KEY = "file";
    private HttpClient mHttpClient;
    private Handler mHandler;

    public FileUploadFacade() {
        mHttpClient = new DefaultHttpClient();
        mHandler = new Handler();
    };

    public FileUploadFacade(HttpClient httpClient) {
        mHttpClient = httpClient;
        mHandler = new Handler();
    }

    public void post(String url, File file, FileUploadCallback callback) {
        post(url, null, file, null, null, callback);
    }

    public void post(String url, String fileKey, File file, FileUploadCallback callback) {
        post(url, fileKey, file, null, null, callback);
    }

    public void post(String url, File file, Map<String, String> params, FileUploadCallback callback) {
        post(url, null, file, null, params, callback);
    }

    public void post(String url, String fileKey, File file, Map<String, String> params, FileUploadCallback callback) {
        post(url, fileKey, file, null, params, callback);
    }

    public void post(final String url, final String fileKey, final File file, final String contentType,
            final Map<String, String> params, final FileUploadCallback callback) {

        if (null == callback) {
            throw new RuntimeException("FileUploadCallback should not be null.");
        }

        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(new Runnable() {
            public void run() {
                try {
                    HttpEntityEnclosingRequestBase request = createRequest(HttpMethod.POST, url);
                    request.setEntity(createEntity(fileKey, file, contentType, params));
                    upload(request, callback);
                } catch (UnsupportedEncodingException e) {
                    callback.onFailure(-1, null, e);
                }
            }
        });
    }

    public void put(String url, File file, FileUploadCallback callback) {
        put(url, null, file, null, null, callback);
    }

    public void put(String url, String fileKey, File file, FileUploadCallback callback) {
        put(url, fileKey, file, null, null, callback);
    }

    public void put(String url, File file, Map<String, String> params, FileUploadCallback callback) {
        put(url, null, file, null, params, callback);
    }

    public void put(String url, String fileKey, File file, Map<String, String> params, FileUploadCallback callback) {
        put(url, fileKey, file, null, params, callback);
    }

    public void put(final String url, final String fileKey, final File file, final String contentType,
            final Map<String, String> params, final FileUploadCallback callback) {

        if (null == callback) {
            throw new RuntimeException("FileUploadCallback should not be null.");
        }

        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(new Runnable() {
            public void run() {
                try {
                    HttpEntityEnclosingRequestBase request = createRequest(HttpMethod.PUT, url);
                    request.setEntity(createEntity(fileKey, file, contentType, params));
                    upload(request, callback);
                } catch (UnsupportedEncodingException e) {
                    callback.onFailure(-1, null, e);
                }
            }
        });
    }

    private HttpEntityEnclosingRequestBase createRequest(HttpMethod method, String url) {
        if (method == HttpMethod.POST) {
            return new HttpPost(url);
        } else {
            return new HttpPut(url);
        }
    }

    private MultipartEntity createEntity(String fileKey, File file, String contentType, Map<String, String> params)
            throws UnsupportedEncodingException {
        MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        FileBody fileBody;
        if (null == contentType) {
            fileBody = new FileBody(file);
        } else {
            fileBody = new FileBody(file, contentType);
        }

        if (null == fileKey) {
            entity.addPart(DEFAULT_FILE_KEY, fileBody);
        } else {
            entity.addPart(fileKey, fileBody);
        }

        if (null != params) {
            for (Map.Entry<String, String> e : params.entrySet()) {
                entity.addPart(e.getKey(), new StringBody(e.getValue()));
            }
        }
        return entity;
    }

    protected void upload(HttpEntityEnclosingRequestBase request, FileUploadCallback callback) {
        int statusCode = -1;
        String responseBody = null;
        try {
            HttpResponse httpResponse = mHttpClient.execute(request);
            StatusLine status = httpResponse.getStatusLine();
            statusCode = status.getStatusCode();
            responseBody = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");

            if (statusCode >= 300) {
                throw new HttpResponseException(status.getStatusCode(), status.getReasonPhrase());
            }

            sendSuccess(callback, statusCode, responseBody);
        } catch (HttpResponseException e) {
            sendFailure(callback, statusCode, responseBody, e);
        } catch (IOException e) {
            sendFailure(callback, statusCode, responseBody, e);
        }
    }

    private void sendSuccess(final FileUploadCallback callback, final int statusCode, final String response) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onSuccess(statusCode, response);
            }
        });
    }

    private void sendFailure(final FileUploadCallback callback, final int statusCode, final String response,
            final Throwable e) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                callback.onFailure(statusCode, response, e);
            }
        });
    }
}
