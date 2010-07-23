package de.sofd.viskit.image3D.util;

import java.util.*;

public class FPSCounter 
{
    
    protected LinkedList<Long> timeQ;
    
    protected long t2;
    
    public FPSCounter()
    {
        timeQ = new LinkedList<Long>();
        t2 = 0;
    }
    
    public void update()
    {
       long t1 = System.currentTimeMillis();
       
       while ( timeQ.peek() != null && ( t1 - timeQ.peek() ) >= 1000  )
       {
           timeQ.poll();
       }
       
       timeQ.add(t1);
       
    }

    public int getFps()
    {
        if ( timeQ.isEmpty() || timeQ.getFirst() == timeQ.getLast() ) return 0;
        return (int)( timeQ.size() * ( 1000.0f / ( timeQ.getLast() - timeQ.getFirst() ) ) );
    }
   
        
}