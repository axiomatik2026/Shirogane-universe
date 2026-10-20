import sn.amado.serein.*;
public class BackgroundTests {
 static int count;
 static void check(boolean ok,String label){count++;if(!ok)throw new AssertionError(label);}
 public static void main(String[] args){
  check(LocalGuardState.shouldRun(true,true,true,3,5),"enabled protection can run");
  check(!LocalGuardState.shouldRun(false,true,true,3,5),"explicit user disable respected");
  check(!LocalGuardState.shouldRun(true,false,true,3,5),"background opt-out respected");
  check(!LocalGuardState.shouldRun(true,true,false,3,5),"accessibility permission required");
  check(!LocalGuardState.shouldRun(true,true,true,0,5),"no selected apps no foreground service");
  check(!LocalGuardState.shouldRun(true,true,true,3,0),"no block terms no foreground service");
  check(LocalGuardState.state(true,true,true,3,5)==LocalGuardState.State.ACTIVE,"connected service active");
  check(LocalGuardState.state(true,true,false,3,5)==LocalGuardState.State.CONNECTING,"not connected never advertised active");
  check(LocalGuardState.state(false,true,true,3,5)==LocalGuardState.State.DISABLED,"disabled state takes precedence");
  check(LocalGuardState.state(true,false,true,3,5)==LocalGuardState.State.PERMISSION_REQUIRED,"permission revocation reflected");
  check(LocalGuardState.state(true,true,true,0,5)==LocalGuardState.State.NO_RULES,"no apps reflected");
  check(LocalGuardState.countTerms("ecchi\n\n hot \r\nhentai")==3,"persisted custom list restored");
  check(LocalGuardState.countTerms(null)==0,"absent terms safe");
  // This process runs with a SecurityManager forbidding every network connection.
  SearchPolicy local=new SearchPolicy("ecchi\nhot classic\nhentai");
  check(local.blocked("ECCHI anime"),"sensitive search blocked with networking prohibited");
  check(local.blocked("hot classic"),"custom phrase blocked without networking");
  check(!local.blocked("anime Naruto"),"ordinary query stays allowed offline");
  System.out.println(count+" offline/background lifecycle assertions passed");
 }
}
