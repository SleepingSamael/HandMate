package com.chej.HandMate.TTS;

import android.content.Context;
import android.media.AudioManager;
import android.os.Environment;
import android.util.Log;

import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizeBag;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 语音合成工具类
 * Created by samael on 2017/8/14.
 */

public class SpeechUtil implements SpeechSynthesizerListener {

    protected static final String tag = "SpeechUtil";
    private SpeechSynthesizer mSpeechSynthesizer;
    private Context context;

    private String mSampleDirPath;
    private static final String SAMPLE_DIR_NAME = "HandMate";
    private static final String SPEECH_FEMALE_MODEL_NAME = "bd_etts_ch_speech_female.dat";
    private static final String SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male.dat";
    private static final String TEXT_MODEL_NAME = "bd_etts_ch_text.dat";

    public SpeechUtil(Context activity) {
        this.context = activity;
        initialEnv();
        startTTS();
    }
    private void initialEnv() {
        if (mSampleDirPath == null) {
            String sdcardPath = Environment.getExternalStorageDirectory().toString();
            mSampleDirPath = sdcardPath + "/" + SAMPLE_DIR_NAME;
        }
        makeDir(mSampleDirPath);
        copyFromAssetsToSdcard(false, SPEECH_FEMALE_MODEL_NAME, mSampleDirPath + "/" + SPEECH_FEMALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, TEXT_MODEL_NAME, mSampleDirPath + "/" + TEXT_MODEL_NAME);
        //copyFromAssetsToSdcard(false, LICENSE_FILE_NAME, mSampleDirPath + "/" + LICENSE_FILE_NAME);
    }

    private void makeDir(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }
    /**
     * 将sample工程需要的资源文件拷贝到SD卡中使用（授权文件为临时授权文件，请注册正式授权）
     *
     * @param isCover 是否覆盖已存在的目标文件
     * @param source
     * @param dest
     */
    private void copyFromAssetsToSdcard(boolean isCover, String source, String dest) {
        File file = new File(dest);
        if (isCover || (!isCover && !file.exists())) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = context.getResources().getAssets().open(source);
                String path = dest;
                fos = new FileOutputStream(path);
                byte[] buffer = new byte[1024];
                int size = 0;
                while ((size = is.read(buffer, 0, 1024)) >= 0) {
                    fos.write(buffer, 0, size);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * 初始化合成相关组件
     */
    // 初始化语音合成客户端并启动
    private void startTTS() {
        this.mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        this.mSpeechSynthesizer.setContext(context);
        this.mSpeechSynthesizer.setSpeechSynthesizerListener(this);
        // 文本模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, mSampleDirPath + "/"
                + TEXT_MODEL_NAME);
        // 声学模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, mSampleDirPath + "/"
                + SPEECH_FEMALE_MODEL_NAME);
        // 请替换为语音开发者平台上注册应用得到的App ID (离线授权)
        this.mSpeechSynthesizer.setAppId("9993937");
        // 请替换为语音开发者平台注册应用得到的apikey和secretkey (在线授权)
        this.mSpeechSynthesizer.setApiKey("Gk7tKvpHWhHUmDIrxAF8XYXF",
                "8e44a2c026d72415bd2d4525c8fc3654");
        setParams();
        // 授权检测接口(只是通过AuthInfo进行检验授权是否成功。)
        // AuthInfo接口用于测试开发者是否成功申请了在线或者离线授权，如果测试授权成功了，可以删除AuthInfo部分的代码（该接口首次验证时比较耗时），不会影响正常使用（合成使用时SDK内部会自动验证授权）
        AuthInfo authInfo = this.mSpeechSynthesizer.auth(TtsMode.MIX);
        // 判断授权信息是否正确，如果正确则初始化语音合成器并开始语音合成，如果失败则做错误处理
        if (authInfo.isSuccess()) {
            Log.e(tag,"百度语音合成示例程序正在运行");
        } else {
            String errorMsg = authInfo.getTtsError().getDetailMessage();
            Log.e(tag,"授权失败 "+ errorMsg);
        }
    }
    /**
     * 开始文本合成并朗读
     */
    public void speak(final String content) {
        new Thread(new Runnable() {
            @Override
            public void run() {
//                 setParams();
                int result = mSpeechSynthesizer.speak(content.toString());
                if (result < 0) {
                    Log.e(tag,"开始合成器失败："+result);
                }
            }
        }).start();
    }

    /**
     * 为语音合成器设置相关参数
     */
    private void setParams() {
        // 发音人（在线引擎），可用参数为0,1,2,3。。。（服务器端会动态增加，各值含义参考文档，以文档说明为准。0--普通女声，1--普通男声，2--特别男声，3--情感男声。。。）
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置Mix模式的合成策略
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "9");//音量，取值范围[0, 9]，数值越大，音量越大
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "5");//朗读语速，取值范围[0, 9]，数值越大，语速越快
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5");//音调，取值范围[0, 9]，数值越大，音量越高
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_AUDIO_ENCODE,
                SpeechSynthesizer.AUDIO_ENCODE_AMR);//音频格式，支持bv/amr/opus/mp3，取值详见随后常量声明
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_AUDIO_RATE,
                SpeechSynthesizer.AUDIO_BITRATE_AMR_15K85);//音频比特率，各音频格式支持的比特率详见随后常量声明
    }
    private void pause() {
        this.mSpeechSynthesizer.pause();
    }

    private void resume() {
        this.mSpeechSynthesizer.resume();
    }

    private void stop() {
        this.mSpeechSynthesizer.stop();
    }

    private void synthesize(String text) {
        //需要合成的文本text的长度不能超过1024个GBK字节。
        int result = this.mSpeechSynthesizer.synthesize(text);
        if (result < 0) {
            Log.e(tag,"error,please look up error code in doc or URL:http://yuyin.baidu.com/docs/tts/122 ");
        }
    }

    private void batchSpeak() {
        List<SpeechSynthesizeBag> bags = new ArrayList<SpeechSynthesizeBag>();
        bags.add(getSpeechSynthesizeBag("123456", "0"));
        bags.add(getSpeechSynthesizeBag("你好", "1"));
        bags.add(getSpeechSynthesizeBag("使用百度语音合成SDK", "2"));
        bags.add(getSpeechSynthesizeBag("hello", "3"));
        bags.add(getSpeechSynthesizeBag("这是一个demo工程", "4"));
        int result = this.mSpeechSynthesizer.batchSpeak(bags);
        if (result < 0) {
            Log.e(tag,"error,please look up error code in doc or URL:http://yuyin.baidu.com/docs/tts/122 ");
        }
    }

    private SpeechSynthesizeBag getSpeechSynthesizeBag(String text, String utteranceId) {
        SpeechSynthesizeBag speechSynthesizeBag = new SpeechSynthesizeBag();
        //需要合成的文本text的长度不能超过1024个GBK字节。
        speechSynthesizeBag.setText(text);
        speechSynthesizeBag.setUtteranceId(utteranceId);
        return speechSynthesizeBag;
    }

    @Override
    public void onSynthesizeStart(String s) {
        Log.e(tag,"onSynthesizeStart utteranceId=" +s);
    }

    @Override
    public void onSynthesizeDataArrived(String utteranceId, byte[] data, int progress) {
        // toPrint("onSynthesizeDataArrived");
    }

    /**
     * 合成正常结束，每句合成正常结束都会回调，如果过程中出错，则回调onError，不再回调此接口
     * @param utteranceId
     */
    @Override
    public void onSynthesizeFinish(String utteranceId) {
        Log.e(tag,"onSynthesizeFinish utteranceId=" + utteranceId);
    }

    /**
     * 播放开始，每句播放开始都会回调
     * @param utteranceId
     */
    @Override
    public void onSpeechStart(String utteranceId) {
        Log.e(tag,"onSpeechStart utteranceId=" + utteranceId);
    }

    @Override
    public void onSpeechProgressChanged(String s, int i) {

    }

    @Override
    public void onSpeechFinish(String s) {

    }

    /**
     * 当合成或者播放过程中出错时回调此接口
     * @param utteranceId
     * @param error 包含错误码和错误信息
     */
    @Override
    public void onError(String utteranceId, SpeechError error) {
        Log.e(tag,"onError error=" + "(" + error.code + ")" + error.description + "--utteranceId=" + utteranceId);
    }
}
