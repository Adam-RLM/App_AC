package com.example.assistcontrol;

public class ScreenItem {
    String Tittle, Description;
    int ScreenImg;

    public ScreenItem(String tittle, String description, int screenImg){
        Tittle = tittle;
        Description = description;
        ScreenImg = screenImg;
    }

    public void setTittle (String tittle){
        Tittle = tittle;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public void setScreenImg(int screenImg) {
        ScreenImg = screenImg;
    }

    public String getTittle() {
        return Tittle;
    }

    public String getDescription() {
        return Description;
    }

    public int getScreenImg() {
        return ScreenImg;
    }
}
