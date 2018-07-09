package com.example.carla.myvocalapplication;

public interface VoiceBaseManager {

        void speak(int requestCode, int speechResourceId, Object... formatArgs);

        void speak(int requestCode, int speechResourceId);

        void listen(int requestCode);

        void shutdown();

        void setVoiceManagerListener(VoiceManager.VoiceManagerListener listener);

}
