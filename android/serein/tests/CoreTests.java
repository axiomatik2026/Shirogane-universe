import sn.amado.serein.*;
import java.io.*;
import java.util.*;
public class CoreTests {
 static int count;
 static void check(boolean yes,String name){count++;if(!yes)throw new AssertionError(name);}
 public static void main(String[] args)throws Exception{
  check(Policy.blocked("www.nhentai.net",""),"hentai subdomain");
  check(Policy.blocked("hanime.tv",""),"hentai host");
  check(Policy.blocked("images.rule34.xxx",""),"rule34");
  check(!Policy.blocked("notpornhub.com",""),"suffix boundary");
  check(!Policy.blocked("wikipedia.org",""),"normal host");
  check(Policy.blocked("sub.example.com","https://example.com/path"),"custom domain");
  check(!Policy.allowedUrl("https://google.com/search?q=hentai",""),"search blocked");
  check(!Policy.allowedUrl("https://google.com/search?q=%70%6frn",""),"encoded search");
  check(!Policy.allowedUrl("http://example.com",""),"cleartext blocked");
  check(!Policy.allowedUrl("intent://example.com",""),"external scheme");
  check(!Policy.allowedUrl("https://safe.org@pornhub.com",""),"userinfo");
  check(Policy.allowedUrl("https://wikipedia.org/wiki/Android",""),"regular document");
  ByteArrayOutputStream b=new ByteArrayOutputStream();b.write(new byte[]{0x12,0x34,1,0,0,1,0,0,0,0,0,0});for(String s:"example.com".split("\\.")){b.write(s.length());b.write(s.getBytes("US-ASCII"));}b.write(new byte[]{0,0,1,0,1});byte[] q=b.toByteArray();
  check(DnsPacket.question(q).equals("example.com"),"question parser");
  byte[] malformed=q.clone();malformed[12]=(byte)255;check(DnsPacket.question(malformed).isEmpty(),"bad DNS label");
  check(DnsPacket.question(Arrays.copyOf(q,q.length-1)).isEmpty(),"truncated DNS");
  byte[] err=DnsPacket.error(q,3);check((err[3]&15)==3 && (err[2]&128)!=0,"NXDOMAIN");
  byte[] req=new byte[28+q.length];req[0]=0x45;req[2]=(byte)(req.length>>8);req[3]=(byte)req.length;req[9]=17;req[12]=10;req[15]=1;req[16]=10;req[19]=2;req[20]=0x12;req[21]=0x34;req[23]=53;req[24]=(byte)((q.length+8)>>8);req[25]=(byte)(q.length+8);System.arraycopy(q,0,req,28,q.length);
  check(DnsPacket.offset(req,req.length)==28,"UDP offset");byte[] response=DnsPacket.reply(req,28,err);
  check(response[15]==2&&response[19]==1,"address swap");check(DnsPacket.u16(response,22)==0x1234,"port swap");
  int sum=0;for(int i=0;i<20;i+=2)sum+=DnsPacket.u16(response,i);while(sum>65535)sum=(sum&65535)+(sum>>16);check(sum==65535,"IP checksum");
  req[6]=0x20;check(DnsPacket.offset(req,req.length)==-1,"fragments rejected");
  Random rng=new Random(37);for(int i=0;i<20000;i++){byte[] random=new byte[rng.nextInt(512)];rng.nextBytes(random);DnsPacket.question(random);DnsPacket.offset(random,random.length);}
  System.out.println(count+" assertions passed; 20,000 malformed packets handled");
 }
}
