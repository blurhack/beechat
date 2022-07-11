package com.beesec.beechat2.live.stats;


import androidx.annotation.NonNull;

import java.util.Locale;
@SuppressWarnings("ALL")
public class LocalStatsData extends StatsData {
    private static final String FORMAT = "Local(%d)\n\n" +
            "%dx%d %dfps\n" +
            "LastMile delay: %d ms\n" +
            "Video tx/rx (kbps): %d/%d\n" +
            "Audio tx/rx (kbps): %d/%d\n" +
            "CPU: app/total %.1f%%/%.1f%%\n" +
            "Quality tx/rx: %s/%s\n" +
            "Loss tx/rx: %d%%/%d%%";

    private int lastMileDelay;
    private int videoSend;
    private int videoRecv;
    private int audioSend;
    private int audioRecv;
    private double cpuApp;
    private double cpuTotal;
    private int sendLoss;
    private int recvLoss;

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(), FORMAT,
                getUid(),
                getWidth(), getHeight(), getFramerate(),
                getLastMileDelay(),
                getVideoSendBitrate(), getVideoRecvBitrate(),
                getAudioSendBitrate(), getAudioRecvBitrate(),
                getCpuApp(), getCpuTotal(),
                getSendQuality(), getRecvQuality(),
                getSendLoss(), getRecvLoss());
    }

    public int getLastMileDelay() {
        return lastMileDelay;
    }

    @SuppressWarnings("unused")
    public void setLastMileDelay(int lastMileDelay) {
        this.lastMileDelay = lastMileDelay;
    }

    public int getVideoSendBitrate() {
        return videoSend;
    }

    @SuppressWarnings("unused")
    public void setVideoSendBitrate(int videoSend) {
        this.videoSend = videoSend;
    }

    public int getVideoRecvBitrate() {
        return videoRecv;
    }

    @SuppressWarnings("unused")
    public void setVideoRecvBitrate(int videoRecv) {
        this.videoRecv = videoRecv;
    }

    public int getAudioSendBitrate() {
        return audioSend;
    }

    @SuppressWarnings("unused")
    public void setAudioSendBitrate(int audioSend) {
        this.audioSend = audioSend;
    }

    public int getAudioRecvBitrate() {
        return audioRecv;
    }

    @SuppressWarnings("unused")
    public void setAudioRecvBitrate(int audioRecv) {
        this.audioRecv = audioRecv;
    }

    public double getCpuApp() {
        return cpuApp;
    }

    @SuppressWarnings("unused")
    public void setCpuApp(double cpuApp) {
        this.cpuApp = cpuApp;
    }

    public double getCpuTotal() {
        return cpuTotal;
    }

    @SuppressWarnings("unused")
    public void setCpuTotal(double cpuTotal) {
        this.cpuTotal = cpuTotal;
    }

    public int getSendLoss() {
        return sendLoss;
    }

    @SuppressWarnings("unused")
    public void setSendLoss(int sendLoss) {
        this.sendLoss = sendLoss;
    }

    public int getRecvLoss() {
        return recvLoss;
    }

    @SuppressWarnings("unused")
    public void setRecvLoss(int recvLoss) {
        this.recvLoss = recvLoss;
    }

}
