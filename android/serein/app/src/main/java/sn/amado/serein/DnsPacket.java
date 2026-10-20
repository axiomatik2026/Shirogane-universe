package sn.amado.serein;
import java.util.Arrays;
/** Bounds-checked IPv4 UDP DNS codec. The TUN only routes our DNS /32. */
public final class DnsPacket {
    public static int u16(byte[] b,int p) { return ((b[p]&255)<<8)|(b[p+1]&255); }
    static void put(byte[] b,int p,int n) { b[p]=(byte)(n>>8); b[p+1]=(byte)n; }
    public static int offset(byte[] p,int n) {
        if(n<40 || (p[0]>>4&15)!=4 || (p[9]&255)!=17) return -1;
        int ihl=(p[0]&15)*4;
        if(ihl<20 || ihl+20>n || u16(p,2)!=n || (u16(p,6)&0x3fff)!=0 || u16(p,ihl+2)!=53) return -1;
        int len=u16(p,ihl+4);
        if(len<20 || ihl+len!=n) return -1;
        return ihl+8;
    }
    public static String question(byte[] q) {
        if(q.length<17 || u16(q,4)!=1 || (q[2]&0x80)!=0) return "";
        StringBuilder h=new StringBuilder(); int p=12;
        while(p<q.length) {
            int len=q[p++]&255;
            if(len==0) return p+4<=q.length ? h.toString() : "";
            if(len>63 || p+len>q.length || h.length()+len>253) return "";
            if(h.length()>0) h.append('.');
            for(int j=0;j<len;j++) { int c=q[p++]&255; if(c<33||c>126) return ""; h.append((char)c); }
        }
        return "";
    }
    public static boolean matchesResponse(byte[] q,byte[] r) {
        if(q.length<17 || r.length<17 || q[0]!=r[0] || q[1]!=r[1] || (r[2]&128)==0) return false;
        byte[] copy=r.clone();copy[2]&=0x7f;
        String a=question(q),b=question(copy);
        if(a.isEmpty() || !a.equalsIgnoreCase(b))return false;
        int end=12;while(end<q.length && q[end]!=0)end+=1+(q[end]&255);
        int other=12;while(other<r.length && r[other]!=0)other+=1+(r[other]&255);
        return end+4<q.length && other+4<r.length && u16(q,end+1)==u16(r,other+1) && u16(q,end+3)==u16(r,other+3);
    }
    public static byte[] error(byte[] q,int rcode) {
        byte[] r=q.clone(); r[2]=(byte)0x81; r[3]=(byte)(0x80|rcode);
        Arrays.fill(r,6,12,(byte)0); return r;
    }
    public static byte[] reply(byte[] request,int offset,byte[] dns) {
        byte[] r=new byte[28+dns.length]; r[0]=0x45; put(r,2,r.length); r[8]=64; r[9]=17;
        System.arraycopy(request,16,r,12,4); System.arraycopy(request,12,r,16,4);
        put(r,20,53); put(r,22,u16(request,offset-8)); put(r,24,8+dns.length);
        System.arraycopy(dns,0,r,28,dns.length);
        int sum=0; for(int i=0;i<20;i+=2) sum+=u16(r,i);
        while((sum>>16)!=0) sum=(sum&65535)+(sum>>16);
        put(r,10,~sum); return r;
    }
}
