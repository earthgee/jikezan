# jikezan
仿制即刻的点赞效果

效果:  
![img](https://github.com/earthgee/jikezan/blob/master/app/other/2017-10-16_19_41_37.gif)

实现：  
图片的缩放通过一个动画控制。  
文本的移动通过另一个动画控制。  
文本的移动是这样设计的:先根据变化的方向（加或减）确定不变的数字，和变的数字。不变的数字比较好绘制。  
对于变的数字，用两个Paint对象分别绘制两个文本，模拟一种淡入淡出的效果，如图：  
![img](https://github.com/earthgee/jikezan/blob/master/app/other/FF0F658B-0552-4E1E-BD15-6D06E47F9138.png)
