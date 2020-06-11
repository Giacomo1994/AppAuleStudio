package com.example.appaulestudio;

public class CalendarAccount {
    private long id;
    private String name;
    private String name_account;
    private String type;
    private String owner;

    public CalendarAccount(long id, String name, String name_account, String type, String owner) {
        this.id = id;
        this.name = name;
        this.name_account = name_account;
        this.type = type;
        this.owner = owner;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName_account() {
        return name_account;
    }

    public void setName_account(String name_account) {
        this.name_account = name_account;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalendarAccount that = (CalendarAccount) o;
        return id == that.id;
    }
}
