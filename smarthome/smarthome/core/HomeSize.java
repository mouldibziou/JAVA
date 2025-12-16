package smarthome.core;

public enum HomeSize{
    SMALL("A lightly equipped home"),
    MEDIUM("A moderately equipped home"),
    LARGE("A highly equipped home"),
    SMART("A fully equipped home");

    private final String description;

    HomeSize(String description){
        this.description = description;
    }

    public String getDescription(){
        return description;
    }
}

