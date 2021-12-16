package com.example.tablemaintain01

class HttpRequester {
    private static BASE_URL = "http://localhost:8081/api/base/"
    String uri

    HttpRequester(String path) {
        this.uri = BASE_URL + path
    }

    String connect(String input) {
        def url = new URL(uri)
        def connection = (HttpURLConnection) url.openConnection()
        connection.setRequestMethod("POST")
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        connection.setDoOutput(true)

        byte[] postData = input.getBytes("utf-8")
        int postDataLength = postData.length
        connection.setRequestProperty("charset", "utf-8")
        connection.setRequestProperty("Content-Length", Integer.toString(postDataLength))

        DataOutputStream wr = new DataOutputStream(connection.getOutputStream())
        wr.write(postData)
        wr.close()
        def result  = connection.inputStream.text
        System.out.println(connection.getResponseCode() + ": " + connection.getResponseMessage())
        System.out.println(result)
        return result
    }
}