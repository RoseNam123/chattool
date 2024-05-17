import javax.sound.sampled.*;

public class AudioTest {
    public static int[] ChangeSound(int[] input){
        int [] frequency = new int[input.length];
        for (int i = 0; i < input.length; i++) {
            //int[] frequencys = {261,293,329,349,392,440,493,523};
            if(1==input[i]){
                frequency[i] = 261;
            }else if (2==input[i]) {
                frequency[i] = 293;
            }else if (3==input[i]) {
                frequency[i] = 329;
            }else if (4==input[i]) {
                frequency[i] = 349;
            }else if (5==input[i]) {
                frequency[i] = 392;
            }else if (6==input[i]) {
                frequency[i] = 440;
            }else if (7==input[i]) {
                frequency[i] = 493;
            }else if (11==input[i]) {
                frequency[i] = 523;
            } else if (0==input[i]) {
                frequency[i] = 0;
            } else if (-7==input[i]) {
                frequency[i] = 231;
            }
        }
        return frequency;
    }
    public static void main(String[] args) {
        //int[] frequencys = {261,293,329,349,392,440,493,523};
        //little star
        //int[] frequencys = {261,261,392,392,440,440,392,0,349,349,329,329,293,293,261,0,392,392,349,349,329,329,293,0,392,392,349,349,329,329,293,0,261,261,392,392,440,440,392,0,349,349,329,329,293,293,261,0};
        int[] a = {1,1,5,5,6,6,5,0,4,4,3,3,2,2,1,0,5,5,4,4,3,3,2,0,5,5,4,4,3,3,2,0,1,1,5,5,6,6,5,0,4,4,3,3,2,2,1};
        //int[] a = {-7,-7,-7,-7};
        int[] frequencys = ChangeSound(a);
        try {
            // 设置提示音的频率和持续时间
            int frequency = 261; // 频率（赫兹）
            int duration = 1000; // 持续时间（毫秒）
            //int repeatCount = 7; // 循环播放次数

            // 获取默认的音频输出设备
            Clip clip = AudioSystem.getClip();

            for (int i = 0; i < frequencys.length; i++) {
                System.out.print(i+"\t");
                frequency=frequencys[i];
                System.out.println(frequency);
                // 创建一个简单的音频样式，使用正弦波作为提示音

                byte[] buffer = new byte[(int) clip.getFormat().getFrameRate() * duration / 1000];
                for (int j = 0; j < buffer.length; j++) {
                    double angle = 2.0 * Math.PI * frequency * j / clip.getFormat().getFrameRate();
                    buffer[j] = (byte) (Math.sin(angle) * 127);
                }

                // 打开音频流并播放
                clip.open(clip.getFormat(), buffer, 0, buffer.length);
                clip.start();

                // 等待提示音播放完毕
                Thread.sleep(500);

                // 关闭音频流
                clip.stop();
                clip.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}