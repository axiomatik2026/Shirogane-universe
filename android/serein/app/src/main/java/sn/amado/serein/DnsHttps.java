package sn.amado.serein;

import java.io.*;
import java.net.URL;
import java.util.Base64;
import javax.net.ssl.HttpsURLConnection;

/** RFC 8484 transport. Android supplies a connection bound to a non-VPN network. */
public final class DnsHttps {
    public interface ConnectionFactory { HttpsURLConnection open(URL url) throws IOException; }
    public static final String ENDPOINT="https://doh.cleanbrowsing.org/doh/adult-filter/";
    public static byte[] query(byte[] question, ConnectionFactory factory) throws IOException {
        if(DnsPacket.question(question).isEmpty()) throw new IOException("Invalid question");
        URL url=new URL(ENDPOINT+"?dns="+Base64.getUrlEncoder().withoutPadding().encodeToString(question));
        HttpsURLConnection connection=factory.open(url);
        try {
            connection.setConnectTimeout(4000);connection.setReadTimeout(5000);
            connection.setInstanceFollowRedirects(false);connection.setUseCaches(false);
            connection.setRequestProperty("Accept","application/dns-message");
            connection.setRequestProperty("Cache-Control","no-store");
            if(connection.getResponseCode()!=200)throw new IOException("DNS HTTPS unavailable");
            String type=connection.getContentType();
            if(type==null || !type.split(";",2)[0].trim().equalsIgnoreCase("application/dns-message"))throw new IOException("Invalid DNS media type");
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            try(InputStream in=connection.getInputStream()) {
                byte[] buffer=new byte[2048];int n;
                while((n=in.read(buffer))!=-1){if(out.size()+n>32000)throw new IOException("DNS reply too large");out.write(buffer,0,n);}
            }
            byte[] reply=out.toByteArray();
            if(!DnsPacket.matchesResponse(question,reply))throw new IOException("Mismatched DNS response");
            return reply;
        } finally { connection.disconnect(); }
    }
    public static byte[] probe() {
        return new byte[]{0x53,0x52,1,0,0,1,0,0,0,0,0,0,7,'e','x','a','m','p','l','e',3,'c','o','m',0,0,1,0,1};
    }
}
