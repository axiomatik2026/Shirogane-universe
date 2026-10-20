import sn.amado.serein.*;
import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.Principal;
import java.security.cert.Certificate;
import java.util.*;

public class HttpsTests {
 static int count;
 static void check(boolean value,String message){count++;if(!value)throw new AssertionError(message);}
 static class Fake extends HttpsURLConnection {
  byte[] body;String type="application/dns-message";int status=200;boolean closed;
  Fake(byte[] body)throws Exception{super(new URL(DnsHttps.ENDPOINT));this.body=body;}
  public void disconnect(){closed=true;}public boolean usingProxy(){return false;}public void connect(){}
  public int getResponseCode(){return status;}public String getContentType(){return type;}
  public InputStream getInputStream(){return new ByteArrayInputStream(body);}
  public String getCipherSuite(){return "test";}public Certificate[] getLocalCertificates(){return null;}public Certificate[] getServerCertificates(){return null;}
  public Principal getPeerPrincipal(){return null;}public Principal getLocalPrincipal(){return null;}
 }
 static void rejected(byte[] q,Fake fake,String label)throws Exception{
  boolean failed=false;try{DnsHttps.query(q,u->fake);}catch(IOException e){failed=true;}
  check(failed,label);check(fake.closed,label+" closes connection");
 }
 public static void main(String[] args)throws Exception{
  byte[] q=DnsHttps.probe(),reply=DnsPacket.error(q,0);reply[7]=1;
  check(DnsPacket.question(q).equals("example.com"),"valid preflight query");
  check(DnsPacket.matchesResponse(q,reply),"matching DNS reply");
  Fake success=new Fake(reply);
  byte[] result=DnsHttps.query(q,u->{check(u.getProtocol().equals("https"),"encrypted transport");check(u.getPath().equals("/doh/adult-filter/"),"adult-only endpoint");check(Arrays.equals(Base64.getUrlDecoder().decode(u.getQuery().substring(4)),q),"DNS GET payload");return success;});
  check(Arrays.equals(reply,result),"valid reply returned");check(success.closed,"success closes connection");check(!success.getInstanceFollowRedirects(),"no redirect to unfiltered endpoint");check(success.getConnectTimeout()==4000 && success.getReadTimeout()==5000,"bounded timeouts");
  Fake bad=new Fake(reply);bad.type="text/html";rejected(q,bad,"HTML cannot masquerade as DNS");
  bad=new Fake(reply);bad.status=503;rejected(q,bad,"server outage rejected");
  byte[] wrong=reply.clone();wrong[0]++;rejected(q,new Fake(wrong),"transaction mismatch rejected");
  wrong=reply.clone();wrong[13]='z';rejected(q,new Fake(wrong),"question mismatch rejected");
  wrong=reply.clone();wrong[wrong.length-3]=28;rejected(q,new Fake(wrong),"question type mismatch rejected");
  rejected(q,new Fake(new byte[]{1,2}),"truncated reply rejected");
  rejected(q,new Fake(new byte[32001]),"oversize reply rejected");
  System.out.println(count+" HTTPS transport assertions passed");
 }
}
