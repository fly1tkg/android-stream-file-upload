android-stream-file-upload
==========================
Travis-CI(master)

[![Build Status](https://travis-ci.org/fly1tkg/android-stream-file-upload.png?branch=master)](https://travis-ci.org/fly1tkg/android-stream-file-upload)

#Description
Simple stream file upload library! You won't worry about "OutOfMemory".

The File upload will be done in background.

#Jar download

https://github.com/fly1tkg/android-stream-file-upload/tree/master/jar

#How to Use
    new FileUploadFacade fileUploadFacade = new FileUploadFacade();
    String url = "http://posturl.com" // post url
    File file = new File("path") // java.io.File you want to upload
    fileUploadFacade.post(url, file, params, new FileUploadCallback() {
        @Override
        public void onSuccess(int code, String response) {
            // if upload success
        }

        @Override
        public void onFailure(int code, String response, Throwable e) {
            // if upload failure
        }
    });
    
# How to create Jar
    mvn clean package
    
or assemble with dependency

    mvn clean compile assembly:single


