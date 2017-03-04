package gonggongjohn.tocker;

public class Face {
    public Attributes attributes;
    public FaceRectangle face_rectangle;

    public static class Attributes{
        public Headpose headpose;

        public static class Headpose{
            public double yaw_angle;
            public double pitch_angle;
            public double roll_angle;

            public double getYawAngle(){
                return yaw_angle;
            }
            public void setYawAngle(double yawangle){
                this.yaw_angle = yawangle;
            }
            public double getPitchAngle(){
                return pitch_angle;
            }
            public void setPitchAngle(double pitchangle){
                this.pitch_angle = pitchangle;
            }
            public double getRollAngle(){
                return roll_angle;
            }
            public void setRollAngle(double rollangle){
                this.roll_angle = rollangle;
            }
        }
        public Headpose getHeadpose(){
            return headpose;
        }
        public void setHeadpose(Headpose head_pose){
            this.headpose = head_pose;
        }
    }

    public static  class FaceRectangle{
        public int width;
        public int top;
        public int left;
        public int height;
        public int getWidth(){
            return width;
        }
        public void setWidth(int Width){
            this.width = Width;
        }
        public int getTop(){
            return top;
        }
        public void setTop(int Top){
            this.top = Top;
        }
        public int getLeft(){
            return left;
        }
        public void setLeft(int Left){
            this.left = Left;
        }
        public int getHeight(){
            return height;
        }
        public void setHeight(int Height){
            this.height = Height;
        }
    }
    public Attributes getAttributes(){
        return attributes;
    }
    public void setAttributes(Attributes attri_butes){
        this.attributes = attri_butes;
    }
}
