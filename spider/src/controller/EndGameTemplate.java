package controller;

public abstract class EndGameTemplate {

  
    public final void execute() {
        beforeEnd();
        logResult();
        
        afterEnd();
    }

    protected void beforeEnd() {}
    protected abstract void logResult();
   
    protected void afterEnd() {}
}
