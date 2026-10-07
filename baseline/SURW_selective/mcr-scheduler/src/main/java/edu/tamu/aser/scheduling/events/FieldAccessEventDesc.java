package edu.tamu.aser.scheduling.events;

public class FieldAccessEventDesc extends EventDesc {

    private String fieldOwner;

    private String fieldName;

    private String fieldDesc;
    
    private int lineNumber;

    public FieldAccessEventDesc(EventType eventType, String fieldOwner, String fieldName, String fieldDesc, int lineNumber) {
        super(eventType);
        if (!(eventType.equals(EventType.READ) || eventType.equals(EventType.WRITE))) {
            throw new IllegalArgumentException("Event type has to be read or write");
        }
        this.fieldOwner = fieldOwner;
        this.fieldName = fieldName;
        this.fieldDesc = fieldDesc;
        this.lineNumber = lineNumber;
    }
    
    public FieldAccessEventDesc(EventType eventType, String fieldOwner, String fieldName, String fieldDesc) {
        this(eventType, fieldOwner, fieldName, fieldDesc, -1);
    }

    public String getFieldOwner() {
        return fieldOwner;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFieldDesc() {
        return fieldDesc;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String toString() {
        return "FieldAccessEvent [eventType=" + getEventType() + ", fieldOwner=" + fieldOwner + ", fieldName=" + fieldName + ", fieldDesc="
                + fieldDesc + ", line=" + lineNumber + "]";
    }

}
