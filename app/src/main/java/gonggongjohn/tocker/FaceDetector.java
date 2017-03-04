package gonggongjohn.tocker;


import com.google.gson.Gson;
import com.megvii.cloud.http.CommonOperate;
import com.megvii.cloud.http.Response;

public class FaceDetector {
    private MainActivity main;
    private CountActivity count;
    private String apiKEY = "s7iWsJnl0ZfAMJu_IZ4V5mnZyinMGz0n";
    private String apiSecrect = "o6USx6dPtPKrC_hTO-znQn4WV1zZbyEF";

    public FaceDetector(CountActivity count)
    {
        this.count = count;
    }

    public FaceDetector(MainActivity main){
        this.main = main;
    }

    public void detectFace(final byte[] bytes) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                CommonOperate op = new CommonOperate(apiKEY,apiSecrect,false);
                try {
                    Response res = op.detectByte(bytes, 0, "headpose");
                    String str = new String(res.getContent());
                    Gson gson = new Gson();
                    FaceDetectResult result = gson.fromJson(str,FaceDetectResult.class);
                    if(!result.getFaces().isEmpty()) {
                        System.out.println("检测到人脸");
                        if(main!=null) {
                            main.gotFace();
                        }
                        if(count!=null){
                            count.gotFaceSuccess(str);
                        }
                    }
                    else{
                        System.out.println("未检测到人脸");
                        if(count!=null){
                            count.gotFaceFail();
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        };

        new Thread(r).start();
    }
}
