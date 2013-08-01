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

package com.fly1tkg.streamfileupload.test;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.awaitility.Awaitility.reset;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipart;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import com.fly1tkg.streamfileupload.FileUploadCallback;
import com.fly1tkg.streamfileupload.FileUploadFacade;

@RunWith(RobolectricTestRunner.class)
public class FileUploadFacadeTest {
    private static final String URL = "http://hogehoge.com";
    private static final String FILE_KEY = "filekey";
    private static final File FILE = new File("./src/test/resources/test.txt");
    private static final String CONTENT_TYPE = "text/plain";
    private static final Map<String, String> PARAMS = new HashMap<String, String>();

    private HttpUriRequest mRequest = null;

    static {
        PARAMS.put("key", "value");
        PARAMS.put("key1", "value1");
    }

    @Before
    public void setup() {
        mRequest = null;
        reset();
    }

    @Test(expected = RuntimeException.class)
    public void checkCallbackIsNull() {
        new FileUploadFacadeTestClass().post(null, null, null);
    }

    @Test
    public void post() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        final FileUploadFacadeTestClass fileUploadFacadeTestClass = new FileUploadFacadeTestClass();
        fileUploadFacadeTestClass.post(URL, FILE, new FileUploadCallback() {
            public void onSuccess(int statusCode, String response) {
                mRequest = fileUploadFacadeTestClass.request;
            }

            public void onFailure(int statusCode, String response, Throwable e) {}
        });
        await().until(isSetHttpUriRequest());

        assert (mRequest instanceof HttpPost);
        assertEquals(URL, mRequest.getURI().toString());

        Map<String, ContentBody> parts = getBodyMap(mRequest);
        assert (parts.containsKey("file"));
        assertEquals(FILE, ((FileBody) parts.get("file")).getFile());
    }

    @Test
    public void post1() throws SecurityException, NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException {
        final FileUploadFacadeTestClass fileUploadFacadeTestClass = new FileUploadFacadeTestClass();
        fileUploadFacadeTestClass.post(URL, FILE_KEY, FILE, new FileUploadCallback() {
            public void onSuccess(int statusCode, String response) {
                mRequest = fileUploadFacadeTestClass.request;
            }

            public void onFailure(int statusCode, String response, Throwable e) {}
        });
        await().until(isSetHttpUriRequest());

        assert (mRequest instanceof HttpPost);
        assertEquals(URL, mRequest.getURI().toString());

        Map<String, ContentBody> parts = getBodyMap(mRequest);
        assert (parts.containsKey(FILE_KEY));
        assertEquals(FILE, ((FileBody) parts.get(FILE_KEY)).getFile());
    }

    @Test
    public void post2() throws SecurityException, NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException, UnsupportedEncodingException {
        final FileUploadFacadeTestClass fileUploadFacadeTestClass = new FileUploadFacadeTestClass();
        fileUploadFacadeTestClass.post(URL, FILE, PARAMS, new FileUploadCallback() {
            public void onSuccess(int statusCode, String response) {
                mRequest = fileUploadFacadeTestClass.request;
            }

            public void onFailure(int statusCode, String response, Throwable e) {}
        });
        await().until(isSetHttpUriRequest());

        assert (mRequest instanceof HttpPost);
        assertEquals(URL, mRequest.getURI().toString());

        Map<String, ContentBody> parts = getBodyMap(mRequest);
        assertEquals(FILE, ((FileBody) parts.get("file")).getFile());
        assertEquals("value", stringBodyToString((StringBody) parts.get("key")));
    }

    @Test
    public void post3() throws SecurityException, NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException {
        final FileUploadFacadeTestClass fileUploadFacadeTestClass = new FileUploadFacadeTestClass();
        fileUploadFacadeTestClass.post(URL, FILE_KEY, FILE, PARAMS, new FileUploadCallback() {
            public void onSuccess(int statusCode, String response) {
                mRequest = fileUploadFacadeTestClass.request;
            }

            public void onFailure(int statusCode, String response, Throwable e) {}
        });
        await().until(isSetHttpUriRequest());

        assert (mRequest instanceof HttpPost);
        assertEquals(URL, mRequest.getURI().toString());

        Map<String, ContentBody> parts = getBodyMap(mRequest);
        assert (parts.containsKey(FILE_KEY));
        assertEquals(FILE, ((FileBody) parts.get(FILE_KEY)).getFile());
        assertEquals("value", stringBodyToString((StringBody) parts.get("key")));
    }

    @Test
    public void post4() throws SecurityException, NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException {
        final FileUploadFacadeTestClass fileUploadFacadeTestClass = new FileUploadFacadeTestClass();
        fileUploadFacadeTestClass.post(URL, FILE_KEY, FILE, CONTENT_TYPE, PARAMS, new FileUploadCallback() {
            public void onSuccess(int statusCode, String response) {
                mRequest = fileUploadFacadeTestClass.request;
            }

            public void onFailure(int statusCode, String response, Throwable e) {}
        });
        await().until(isSetHttpUriRequest());

        assert (mRequest instanceof HttpPost);
        assertEquals(URL, mRequest.getURI().toString());

        Map<String, ContentBody> parts = getBodyMap(mRequest);
        assert (parts.containsKey(FILE_KEY));
        FileBody fileBody = (FileBody) parts.get(FILE_KEY);
        assertEquals(FILE, fileBody.getFile());
        assertEquals(CONTENT_TYPE, fileBody.getMimeType());
        assertEquals("value", stringBodyToString((StringBody) parts.get("key")));
    }
    @Test
    public void put() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        final FileUploadFacadeTestClass fileUploadFacadeTestClass = new FileUploadFacadeTestClass();
        fileUploadFacadeTestClass.put(URL, FILE, new FileUploadCallback() {
            public void onSuccess(int statusCode, String response) {
                mRequest = fileUploadFacadeTestClass.request;
            }

            public void onFailure(int statusCode, String response, Throwable e) {}
        });
        await().until(isSetHttpUriRequest());

        assert (mRequest instanceof HttpPost);
        assertEquals(URL, mRequest.getURI().toString());

        Map<String, ContentBody> parts = getBodyMap(mRequest);
        assert (parts.containsKey("file"));
        assertEquals(FILE, ((FileBody) parts.get("file")).getFile());
    }

    @Test
    public void put1() throws SecurityException, NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException {
        final FileUploadFacadeTestClass fileUploadFacadeTestClass = new FileUploadFacadeTestClass();
        fileUploadFacadeTestClass.put(URL, FILE_KEY, FILE, new FileUploadCallback() {
            public void onSuccess(int statusCode, String response) {
                mRequest = fileUploadFacadeTestClass.request;
            }

            public void onFailure(int statusCode, String response, Throwable e) {}
        });
        await().until(isSetHttpUriRequest());

        assert (mRequest instanceof HttpPost);
        assertEquals(URL, mRequest.getURI().toString());

        Map<String, ContentBody> parts = getBodyMap(mRequest);
        assert (parts.containsKey(FILE_KEY));
        assertEquals(FILE, ((FileBody) parts.get(FILE_KEY)).getFile());
    }

    @Test
    public void put2() throws SecurityException, NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException, UnsupportedEncodingException {
        final FileUploadFacadeTestClass fileUploadFacadeTestClass = new FileUploadFacadeTestClass();
        fileUploadFacadeTestClass.put(URL, FILE, PARAMS, new FileUploadCallback() {
            public void onSuccess(int statusCode, String response) {
                mRequest = fileUploadFacadeTestClass.request;
            }

            public void onFailure(int statusCode, String response, Throwable e) {}
        });
        await().until(isSetHttpUriRequest());

        assert (mRequest instanceof HttpPost);
        assertEquals(URL, mRequest.getURI().toString());

        Map<String, ContentBody> parts = getBodyMap(mRequest);
        assertEquals(FILE, ((FileBody) parts.get("file")).getFile());
        assertEquals("value", stringBodyToString((StringBody) parts.get("key")));
    }

    @Test
    public void put3() throws SecurityException, NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException {
        final FileUploadFacadeTestClass fileUploadFacadeTestClass = new FileUploadFacadeTestClass();
        fileUploadFacadeTestClass.put(URL, FILE_KEY, FILE, PARAMS, new FileUploadCallback() {
            public void onSuccess(int statusCode, String response) {
                mRequest = fileUploadFacadeTestClass.request;
            }

            public void onFailure(int statusCode, String response, Throwable e) {}
        });
        await().until(isSetHttpUriRequest());

        assert (mRequest instanceof HttpPost);
        assertEquals(URL, mRequest.getURI().toString());

        Map<String, ContentBody> parts = getBodyMap(mRequest);
        assert (parts.containsKey(FILE_KEY));
        assertEquals(FILE, ((FileBody) parts.get(FILE_KEY)).getFile());
        assertEquals("value", stringBodyToString((StringBody) parts.get("key")));
    }

    @Test
    public void put4() throws SecurityException, NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException {
        final FileUploadFacadeTestClass fileUploadFacadeTestClass = new FileUploadFacadeTestClass();
        fileUploadFacadeTestClass.put(URL, FILE_KEY, FILE, CONTENT_TYPE, PARAMS, new FileUploadCallback() {
            public void onSuccess(int statusCode, String response) {
                mRequest = fileUploadFacadeTestClass.request;
            }

            public void onFailure(int statusCode, String response, Throwable e) {}
        });
        await().until(isSetHttpUriRequest());

        assert (mRequest instanceof HttpPost);
        assertEquals(URL, mRequest.getURI().toString());

        Map<String, ContentBody> parts = getBodyMap(mRequest);
        assert (parts.containsKey(FILE_KEY));
        FileBody fileBody = (FileBody) parts.get(FILE_KEY);
        assertEquals(FILE, fileBody.getFile());
        assertEquals(CONTENT_TYPE, fileBody.getMimeType());
        assertEquals("value", stringBodyToString((StringBody) parts.get("key")));
    }

    private Callable<Boolean> isSetHttpUriRequest() {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return mRequest != null;
            }
        };
    }

    private Map<String, ContentBody> getBodyMap(HttpUriRequest request)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        HttpEntityEnclosingRequestBase httpRequest = (HttpEntityEnclosingRequestBase) request;
        MultipartEntity entity = (MultipartEntity) httpRequest.getEntity();
        Field multipartField = MultipartEntity.class.getDeclaredField("multipart");
        multipartField.setAccessible(true);
        HttpMultipart httpMultipart = (HttpMultipart) multipartField.get(entity);
        List<FormBodyPart> bodyParts = httpMultipart.getBodyParts();
        Map<String, ContentBody> parts = new HashMap<String, ContentBody>();
        for (FormBodyPart part : bodyParts) {
            parts.put(part.getName(), part.getBody());
        }
        return parts;
    }

    private String stringBodyToString(StringBody stringBody)
            throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {
        Field content = StringBody.class.getDeclaredField("content");
        content.setAccessible(true);
        return new String(((byte[]) content.get(stringBody)));
    }

    private class FileUploadFacadeTestClass extends FileUploadFacade {
        HttpUriRequest request;

        @Override
        protected void upload(HttpEntityEnclosingRequestBase request, FileUploadCallback callback) {
            this.request = request;
            callback.onSuccess(0, null);
        }
    }
}
