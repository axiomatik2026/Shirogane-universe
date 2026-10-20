import java.security.Permission;
public class NoNetworkRunner {
 @SuppressWarnings("removal")
 public static void main(String[] args){
  System.setSecurityManager(new SecurityManager(){
   @Override public void checkPermission(Permission p){}
   @Override public void checkConnect(String host,int port){throw new SecurityException("Networking prohibited in offline tests");}
   @Override public void checkListen(int port){throw new SecurityException("Networking prohibited in offline tests");}
   @Override public void checkAccept(String host,int port){throw new SecurityException("Networking prohibited in offline tests");}
  });
  boolean denied=false;try{new java.net.Socket("127.0.0.1",9);}catch(SecurityException e){denied=true;}catch(Exception e){throw new AssertionError(e);}
  if(!denied)throw new AssertionError("Network sandbox not active");
  BackgroundTests.main(args);
 }
}
