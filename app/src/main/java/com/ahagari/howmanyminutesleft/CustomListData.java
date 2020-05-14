package com.ahagari.howmanyminutesleft;

public class CustomListData
{

    private String textData;
    private String contentData;
    private String listTextIndex;
    private String listTextTrainType;
    private String listTextDest;

    public String getListTextIndex() {
        return listTextIndex;
    }

    public void setListTextIndex(String listTextIndex) {
        this.listTextIndex = listTextIndex;
    }

    public String getListTextTrainType() {
        return listTextTrainType;
    }

    public void setListTextTrainType(String listTextTrainType) {
        this.listTextTrainType = listTextTrainType;
    }

    public String getListTextDest() {
        return listTextDest;
    }

    public void setListTextDest(String listTextDest) {
        this.listTextDest = listTextDest;
    }

    public String getListTextTime() {
        return listTextTime;
    }

    public void setListTextTime(String listTextTime) {
        this.listTextTime = listTextTime;
    }

    public String getListTextCountTime() {
        return listTextCountTime;
    }

    public void setListTextCountTime(String listTextCountTime) {
        this.listTextCountTime = listTextCountTime;
    }

    private String listTextTime;
    private String listTextCountTime;


    public String getContentData() {
        return contentData;
    }

    public void setContentData(String contentData) {
        this.contentData = contentData;
    }

    public void setTextData(String textData)
    {
        this.textData = textData;
    }
    public String getTextData()
    {
        return textData;
    }


}