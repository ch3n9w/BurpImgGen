package burp;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestHeader {
    private String boundary;
    private List<String> headers;

    public RequestHeader(IRequestInfo ri) {
        headers = ri.getHeaders();
    }

    public Boolean isPost() {
        Boolean havePOST = false;
        Boolean haveContentType = false;
        Boolean haveContentLength = false;

        for (int i=0;i<headers.size();i++) {
            String header = headers.get(i);
            if (header.contains("POST")) havePOST = true;
            // WARNING: only application/x-www-form-urlencoded is support
            if (header.contains("Content-Type") & header.contains("application/x-www-form-urlencoded")) haveContentType = true;
            if (header.contains("Content-Length")) haveContentLength = true;
        }
        if (!(havePOST & haveContentType & haveContentLength )) return false;
        return true;
    }

    // HACK: the content length will be updated with method buildHttpMessage
    public void setHeaders() {
        for (int i=0;i<headers.size();i++) {
            String header = headers.get(i);
            //stdout.println(header);
            if (header.contains("Content-Type")) {
                setBoundary();
                header = "Content-Type: multipart/form-data; boundary=" + boundary;
            } 
            // append header to new request
            // the content-length header will be added after body length count
            // if (!header.contains("Content-Length")) newHeaders.append(header).append("\r\n");
            headers.set(i, header);
        }
    }

    // Set content-length to request headers;
    /* public void setCL(int length) {
        String header = "Content-Length: " + length;
        newHeaders.append(header);
        newHeaders.append("\r\n");
    } */


    private Set<String> identifiers = new HashSet<>();
    private void setBoundary(){
        String prefix = "---------------------------";
        String lexicon = "12345674890";
        Random rand = new java.util.Random();
        StringBuilder builder = new StringBuilder();
        while (builder.toString().length() == 0) {
            int length = rand.nextInt(30);
            for (int i = 0; i < length; i++) {
                builder.append(lexicon.charAt(rand.nextInt(lexicon.length())));
            }
            if (identifiers.contains(builder.toString())) {
                builder = new StringBuilder();
            }
        }
        builder.insert(0, prefix);
        boundary = builder.toString();
    }

    public List<String> getNewHeaders() {
        return headers;
    }

    public String getBoundary() {
        return boundary;
    }

}
