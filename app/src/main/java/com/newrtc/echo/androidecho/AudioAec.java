package com.newrtc.echo.androidecho;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.util.Log;

public class AudioAec 
{
	private AcousticEchoCanceler m_canceler = null;
	private Recorder m_recorder = null;
	private player   m_player = null; 
	 

    public static boolean chkNewDev()
    {
          return android.os.Build.VERSION.SDK_INT >= 16;
    }
    
	public static boolean isDeviceSupport()
	{
        return AcousticEchoCanceler.isAvailable();
	}

	public boolean initAEC(int audioSession)
	{
		if (m_canceler != null)
		{
			return false;
		}
		m_canceler = AcousticEchoCanceler.create(audioSession);
		m_canceler.setEnabled(true);
		return m_canceler.getEnabled();
	}
	
	public boolean setAECEnabled(boolean enable)
	{
		if (null == m_canceler)
		{
			return false;
		}
		m_canceler.setEnabled(enable);
		return m_canceler.getEnabled();
	}
	
	public boolean release()
	{
	    if (null == m_canceler)
	    {
	        return false;
	    }
	    m_canceler.setEnabled(false);
	    m_canceler.release();
	    return true;
	}
	
	public int StartRecorderAndPlayer()
	{
		int iRet = 0;
		m_recorder = new Recorder();
		
		iRet = m_recorder.InitAudioRecord();
		if(iRet < 0)
		{
			return -1;
		}
		
		if(isDeviceSupport())
		{
			if(initAEC(m_recorder.GetSessionId()))
			{
				setAECEnabled(true);
			}
		}
		
		m_player = new player();
		if(iRet < 0)
		{
			return -1;
		}
		iRet = m_player.InitAudioTrack();
		if(iRet < 0)
		{
			return -1;
		}
		
		m_player.StartAudioTrack(); //start player
		m_recorder.StartAudioRecord(); //start recorder
		

		return 0;
	}
	
	public int StopRecorderAndPlayer()
	{
		return 0;
	}
	
	
	
	
	class Recorder 
	{
		AudioRecord m_audioRecord = null;
		Thread m_audioWorker = null;
		int m_sampleRateInHz = 48000;  
		int m_channelConfig  =  AudioFormat.CHANNEL_IN_STEREO;   
		int m_audioFormat    = AudioFormat.ENCODING_PCM_16BIT; 
		int m_bitRate = 64000;
		int m_bufferSizeInBytes = 0;
		
		short audioData[] = null;
		
		int InitAudioRecord()
		{
			m_bufferSizeInBytes = AudioRecord.getMinBufferSize(m_sampleRateInHz,  m_channelConfig, m_audioFormat);  	
			if (chkNewDev())
			{
				m_audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, m_sampleRateInHz, m_channelConfig, m_audioFormat, m_bufferSizeInBytes);
			}
			else
			{
				m_audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, m_sampleRateInHz, m_channelConfig, m_audioFormat, m_bufferSizeInBytes);
			}
			
			int packSize = Math.min(960, m_bufferSizeInBytes*2);
			audioData = new short[packSize];
			return 0;
		}
		
		public int GetSessionId()
		{
			return m_audioRecord.getAudioSessionId();
		}
		
		int StartAudioRecord()
		{
			Thread m_audioWorker = new Thread(new Runnable() 
			{
	            @Override
	            public void run() 
	            {
	            	ReadMic();
	            }
	        });
			m_audioWorker.start();
			
			return 0;
		}
		
		void ReadMic()
		{
			if(m_audioRecord == null)
			{
				return ;
			}
	        m_audioRecord.startRecording();
	        while (!Thread.interrupted()) 
	        {  	
	        	Log.d("TAG", "ReadMic");
	            int size = m_audioRecord.read(audioData, 0, audioData.length);
	            if (size <= 0) 
	            {  
	                break;
	            }
	            m_player.PlayAudio(audioData, audioData.length);
	        }    
		}
	}
	
	class player
	{
		private AudioTrack m_audioTrack = null;
		
		int InitAudioTrack()
		{
			int m_sampleRateInHz = 48000;  
			int m_channelConfig  =  AudioFormat.CHANNEL_IN_STEREO;   
			int m_audioFormat    = AudioFormat.ENCODING_PCM_16BIT; 
			int m_bufferSizeInBytes = 1024*4;
			
			if (chkNewDev() && m_recorder != null)
			{
				m_audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, m_sampleRateInHz, m_channelConfig, m_audioFormat, m_bufferSizeInBytes, AudioTrack.MODE_STREAM, m_recorder.GetSessionId());
			}
			else
			{
				m_audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, m_sampleRateInHz, m_channelConfig, m_audioFormat, m_bufferSizeInBytes, AudioTrack.MODE_STREAM);
			}
			return 0;
		}
		
		int StartAudioTrack()
		{
			m_audioTrack.play();
			return 0;
		}
		
		public int PlayAudio(short[] audioData, int sizeInShort)
		{
			m_audioTrack.write(audioData, 0, sizeInShort);
			return 0;
		}
	}
	
}
