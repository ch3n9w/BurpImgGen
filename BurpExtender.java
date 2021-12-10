package burp;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;

public class BurpExtender implements IBurpExtender, IContextMenuFactory {
    private IExtensionHelpers helpers;
    private final static String NAME = "BurpImgGen";
    private final static String GEN_PNG = "random PNG";
    private final static String GEN_JPG = "random JPG";
    private final static String GEN_GIF = "random GIF";
    private final static String GEN_CUSTOM = "custom contents";
    private PrintWriter stdout;
    private PrintWriter stderr;

    @java.lang.Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
        
        callbacks.setExtensionName(NAME);
        helpers = callbacks.getHelpers();
        callbacks.registerContextMenuFactory((IContextMenuFactory) this);
        stdout = new PrintWriter(callbacks.getStdout(), true);
        stderr = new PrintWriter(callbacks.getStderr(), true);
        stdout.println("img generater loaded");
    }

    @Override
    public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation) {
        final IHttpRequestResponse message = invocation.getSelectedMessages()[0];
        if (message == null) return null;

        JMenuItem pngItem = new JMenuItem(GEN_PNG);
        pngItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try{
                    message.setRequest(img2Body(message, "png"));
                } catch (Exception e) {
                    stderr.println(e.toString());
                }
            }
        });
        JMenuItem jpgItem = new JMenuItem(GEN_JPG);
        jpgItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    message.setRequest(img2Body(message, "jpg"));
                } catch (Exception e) {
                    stderr.println(e.toString());
                }
            }
        });
        JMenuItem gifItem = new JMenuItem(GEN_GIF);
        gifItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    message.setRequest(img2Body(message, "gif"));
                } catch (Exception e) {
                    e.printStackTrace();
                    stderr.println(e.getStackTrace()[0].getLineNumber());
                    stderr.println(e.getStackTrace()[0].getFileName());
                }
            }
        });
        // TODO: add custom file upload
        /* JMenuItem customItem = new JMenuItem(GEN_CUSTOM);
        customItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
            }
        }); */
        // return Arrays.asList(pngItem,jpgItem,gifItem,customItem);
        return Arrays.asList(pngItem,jpgItem,gifItem);
    }

    public byte[] img2Body(IHttpRequestResponse message, String imgType) throws Exception {
        StringBuilder newRequest = new StringBuilder();
        String boundary;

//        StringBuilder newReq = new StringBuilder("POST ");
        IRequestInfo ri = helpers.analyzeRequest(message);

        RequestHeader newHeaders = new RequestHeader(ri);
        // NOTE: the request should be application/x-www-form-urlencoded POST request
        if (!newHeaders.isPost()) {
            stderr.println("Only for application/x-www-form-urlencoded !");
            return null;
        }
        // NOTE: set new headers without content length
        newHeaders.setHeaders();
        // NOTE: get boundary and pass it into body generation
        boundary = newHeaders.getBoundary();
        stdout.println(boundary);

        RequestBody newBody = new RequestBody(ri, boundary, imgType);
        newBody.setFormData();

        stdout.println("build new http message");
        return helpers.buildHttpMessage(newHeaders.getNewHeaders(), newBody.getFormData());
    }


}
