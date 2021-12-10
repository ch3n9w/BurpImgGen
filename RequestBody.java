package burp;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;

import burp.IParameter;
import burp.IRequestInfo;

import javax.imageio.ImageIO;

public class RequestBody {
    private String boundary;
    private StringBuilder newBody;
    private ArrayList<IParameter> bodyParameters;
    private String imgType; 

    public RequestBody(IRequestInfo ri, String boundary, String imgType) throws Exception{
        bodyParameters = new ArrayList<IParameter>();
        newBody = new StringBuilder();
        this.boundary = boundary;
        this.imgType = imgType;
        List<IParameter> parameters = ri.getParameters();
        int bodyOffset;
        bodyOffset = ri.getBodyOffset();
        for (int index = 0; index < parameters.size(); index++) {
            IParameter parameter = parameters.get(index);
            if (parameter.getNameStart() >= bodyOffset) {
                bodyParameters.add(parameter);
            }

        }
    }

    // TODO: limit content type ==> only www-data
    public void setFormData() throws IOException {
        for (int index = 0; index < bodyParameters.size(); index++) {
            IParameter parameter = bodyParameters.get(index);
            newBody.append(boundary);
            newBody.append("\r\n");
            newBody.append(String.format("Content-Disposition: form-data; name=\"%s\"\r\n\r\n", parameter.getName()));
            newBody.append(parameter.getValue());
            newBody.append("\r\n");
        }
        newBody.append(boundary);
        newBody.append("\r\n");
        newBody.append(String.format("Content-Disposition: form-data; name=\"file\"; filename=\"%s\"\r\n", nameGenerate(imgType)));
        newBody.append(String.format("Content-Type: image/%s\r\n\r\n",imgType));
        newBody.append(new String(imgGenerate(imgType)));
        newBody.append("\r\n");
        newBody.append(boundary + "--" + "\r\n\r\n");
    }

    public byte[] getFormData() {
        return newBody.toString().getBytes();
    }

    public byte[] imgGenerate(String imgType) throws IOException {
        int width = 5;
        int height = 5;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        File f = null;
        for (int x=0;x<width;x++) {
            for (int y=0;y<height;y++){
                int a = (int) (Math.random() * 256);
                int r = (int) (Math.random() * 256);
                int g = (int) (Math.random() * 256);
                int b = (int) (Math.random() * 256);
                int p = (a<<24) | (r<<16) | (g<<8) | b;
                img.setRGB(x, y, p);
            }
        }
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(img, imgType, byteArrayOutputStream);
            System.out.println(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Set<String> identifiers = new HashSet<>();

    public String nameGenerate(String imgType) {
        String lexicon = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz12345674890";
        Random rand = new java.util.Random();
        StringBuilder builder = new StringBuilder();
        while (builder.toString().length() == 0) {
            int length = rand.nextInt(20);
            for (int i = 0; i < length; i++) {
                builder.append(lexicon.charAt(rand.nextInt(lexicon.length())));
            }
            if (identifiers.contains(builder.toString())) {
                builder = new StringBuilder();
            }
        }
        builder.append(imgType);
        return builder.toString();
    }

}
