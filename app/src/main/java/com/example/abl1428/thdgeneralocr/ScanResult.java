package com.example.abl1428.thdgeneralocr;

/**
 * Created by ABL1428 on 7/30/2015.
 */
public class ScanResult {
    private String textContent;
    private float confidence;
    private int[] bounds;

    public ScanResult(String textContent, float confidence, int[] bounds) {
        this.textContent = textContent;
        this.confidence = confidence;
        this.bounds = bounds;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public int[] getBounds() {
        return bounds;
    }

    public void setBounds(int[] bounds) {
        this.bounds = bounds;
    }
}
