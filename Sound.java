package Tetris.main;

import java.net.URL;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.FloatControl;

public class Sound {
    
    Clip musicClip;
    URL url[] = new URL[10];

    public Sound(){
        url[0] = getClass().getResource("/Tetris/resources/sounds/delete line.wav");
        url[1] = getClass().getResource("/Tetris/resources/sounds/gameover.wav");
        url[2] = getClass().getResource("/Tetris/resources/sounds/rotation.wav");
        url[3] = getClass().getResource("/Tetris/resources/sounds/touch floor.wav");
        url[4] = getClass().getResource("/Tetris/resources/sounds/white-labyrinth-active.wav");
    }

    public void playSound(int i, boolean bgMusic){
        try{
            AudioInputStream ais = AudioSystem.getAudioInputStream(url[i]);
            Clip clip = AudioSystem.getClip();

            if(bgMusic){
                musicClip = clip;
            }

            clip.open(ais);

            if(bgMusic){
                setVolume(clip, -10.0f);
            }

            clip.addLineListener(new LineListener() {
                @Override
                public void update(LineEvent event) {
                    if(event.getType() == Type.STOP){
                        clip.close();
                    }
                }               
            });

            ais.close();
            clip.start();
        }
        catch(Exception e){}
    }

    public void setVolume(Clip clip, float volume){
        if(clip.isControlSupported(FloatControl.Type.MASTER_GAIN)){
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(volume);
        }
    }

    public void loop(){
        musicClip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stop(){
        musicClip.stop();
        musicClip.close();
    }
}
