package sn.amado.serein;

/** A busy feed cannot keep postponing a scan forever. Used on the service's main thread. */
public final class ScanGate {
    private boolean queued;
    public boolean enqueue(){if(queued)return false;queued=true;return true;}
    public void consumed(){queued=false;}
}
